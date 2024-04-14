package hu.bme.vik.plugins

import hu.bme.vik.model.UserSession
import hu.bme.vik.repository.CredentialRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Application.configureAuthentication() {
    val repository by inject<CredentialRepository>()

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 300
        }
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                val user = repository.findUserByUsername(credentials.name)
                if (user?.hashedPassword?.let { repository.verifyPassword(credentials.password, it) } == true) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            skipWhen { call ->
                call.sessions.get<UserSession>() != null
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}