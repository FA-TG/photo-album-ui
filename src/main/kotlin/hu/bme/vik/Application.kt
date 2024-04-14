package hu.bme.vik

import hu.bme.vik.model.UserSession
import hu.bme.vik.plugins.configureFormLogin
import hu.bme.vik.plugins.configureRouting
import hu.bme.vik.plugins.configureStaticRouting
import hu.bme.vik.repository.PictureRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    Config.text = environment.config.property("application.text").getString()
    val bucketName = environment.config.property("application.bucketName").getString()
    val mongoClient = KMongo.createClient(
        environment.config.property("application.connectionString").getString()
    )
    val database = mongoClient.getDatabase(
        environment.config.property("application.databaseName").getString()
    )


    install(Koin) {
        slf4jLogger()
        modules(
            org.koin.dsl.module {
                single { PictureRepository(database, bucketName) }
            }
        )
    }

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600
        }
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Credentials are not valid")
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                if(session.name.startsWith("admin")) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }

    configureRouting()
    configureFormLogin()
    configureStaticRouting()

    routing {
        get("/health") {
            call.respond("Up and running.")
        }
    }
}
