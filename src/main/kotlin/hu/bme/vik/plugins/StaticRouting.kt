package hu.bme.vik.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureStaticRouting() {
    routing {
        staticResources("/style", "css")
    }
}