package hu.bme.vik.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO


fun ByteArray.toBufferedImage(): BufferedImage {
    val bais = ByteArrayInputStream(this)
    try {
        return ImageIO.read(bais)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
}

fun BufferedImage.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(this, "JPEG", byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

private const val PATTERN_FORMAT = "uuuu-MM-dd hh:mm"
fun Instant.format() = DateTimeFormatter.ofPattern(PATTERN_FORMAT)
    .withZone(ZoneId.systemDefault())
    .format(this)
