package hu.bme.vik.plugins

import hu.bme.vik.templates.LayoutTemplate
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respondHtmlTemplate(LayoutTemplate(5)) {}
        }
        get("/{it}") {
            val iterator = call.parameters["it"]!!.toInt()
            call.respondHtmlTemplate(LayoutTemplate(iterator)) {}
        }
    }
}