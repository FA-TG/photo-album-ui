package hu.bme.vik

import hu.bme.vik.plugins.configureRouting
import hu.bme.vik.plugins.configureStaticRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Config.text = environment.config.property("application.text").getString()

    configureRouting()
    configureStaticRouting()

    routing {
        get("/health") {
            call.respond("Up and running.")
        }
    }
}
