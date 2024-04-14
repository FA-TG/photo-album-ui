package hu.bme.vik

import hu.bme.vik.plugins.configureAuthentication
import hu.bme.vik.plugins.configureFormLogin
import hu.bme.vik.plugins.configureRouting
import hu.bme.vik.plugins.configureStaticRouting
import hu.bme.vik.repository.CredentialRepository
import hu.bme.vik.repository.PictureRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Config.text = environment.config.property("application.text").getString()
    val bucketName = environment.config.property("application.bucketName").getString()
    val collectionName = environment.config.property("application.collectionName").getString()
    val mongoClient = KMongo.createClient(
        environment.config.property("application.connectionString").getString()
    )
    val database = mongoClient.getDatabase(
        environment.config.property("application.databaseName").getString()
    )
    val coroutineDatabase = mongoClient.coroutine.getDatabase(
        environment.config.property("application.databaseName").getString()
    )


    install(Koin) {
        slf4jLogger()
        modules(
            org.koin.dsl.module {
                single { PictureRepository(database, bucketName) }
                single { CredentialRepository(coroutineDatabase, collectionName) }
            }
        )
    }

    configureAuthentication()
    configureRouting()
    configureFormLogin()
    configureStaticRouting()

    routing {
        get("/health") {
            call.respond("Up and running.")
        }
    }
}
