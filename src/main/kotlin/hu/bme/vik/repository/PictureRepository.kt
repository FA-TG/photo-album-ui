package hu.bme.vik.repository

import com.mongodb.reactivestreams.client.MongoDatabase
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets
import hu.bme.vik.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import org.litote.kmongo.bson
import org.litote.kmongo.coroutine.coroutine
import org.reactivestreams.Publisher
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO


class PictureRepository(
    database: MongoDatabase,
    bucketName: String
) {
    private val bucket = GridFSBuckets.create(database, bucketName)

    suspend fun upload(bufferedImage: BufferedImage, fileName: String) {
        val extension = fileName.substringAfterLast('.', "")

        val outputStream = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            ImageIO.write(bufferedImage, extension, outputStream)
        }

        val byteBuffer = ByteBuffer.wrap(outputStream.toByteArray())

        val content: Publisher<ByteBuffer> = Publisher<ByteBuffer> {
            it.onNext(byteBuffer)
            it.onComplete()
        }
        bucket.uploadFromPublisher(fileName, content).coroutine.consumeEach { println(it) }
    }

    suspend fun getAllPicture() = bucket
        .find()
        .asFlow()
        .map {
            Post(
                it.filename.toString(),
                it.uploadDate.toInstant()
            )
        }.toList()

    suspend fun getByName(name: String) = bucket
        .find("{ filename : '$name' }".bson)
        .asFlow()
        .map {
            Post(
                it.filename.toString(),
                it.uploadDate.toInstant()
            )
        }.firstOrNull()

    suspend fun getPicture(id: String): ByteArray {
        val byteBuffers: MutableList<ByteBuffer> = mutableListOf()
        bucket
            .downloadToPublisher(id)
            .coroutine
            .consumeEach {
                byteBuffers.add(it)
            }

        val byteArrayOutputStream = ByteArrayOutputStream()
        byteBuffers.forEach { byteArrayOutputStream.write(it.array()) }
        return byteArrayOutputStream.toByteArray()
    }

    suspend fun deletePicture(name: String) {
        val id = bucket
            .find("{ filename : '$name' }".bson)
            .asFlow()
            .map {
                it.objectId
            }.firstOrNull()

        id?.let {
            bucket.delete(it).coroutine.toList()
        }
    }
}