package hu.bme.vik.plugins

import hu.bme.vik.model.UserSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Application.configureFormLogin() {
    routing {
        get("/login") {
            call.respondHtml {
                body {
                    form(action = "/login", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                        p {
                            +"Username:"
                            textInput(name = "username")
                        }
                        p {
                            +"Password:"
                            passwordInput(name = "password")
                        }
                        p {
                            submitInput { value = "Login" }
                        }
                    }
                }
            }
        }
        authenticate("auth-form") {
            post("/login") {
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                call.sessions.set(UserSession(name = userName, count = 1))
            }
        }
    }
}