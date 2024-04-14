package hu.bme.vik.plugins

import hu.bme.vik.model.UserSession
import hu.bme.vik.repository.CredentialRepository
import hu.bme.vik.templates.LayoutTemplate
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.koin.ktor.ext.inject

fun Application.configureFormLogin() {
    val repository by inject<CredentialRepository>()

    routing {
        get("/login") {
            call.respondHtmlTemplate(LayoutTemplate(call.sessions.get<UserSession>())) {
                articleTitle {
                    +"Login"
                }
                content {
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
        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/list")
        }
        authenticate("auth-form") {
            post("/login") {
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                call.sessions.set(UserSession(name = userName, count = 1))
                call.respondRedirect("/list")
            }
        }
        route("/register") {
            get {
                call.respondHtmlTemplate(LayoutTemplate(call.sessions.get<UserSession>())) {
                    articleTitle {
                        +"Register"
                    }
                    content {
                        form(action = "/register", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                            p {
                                +"Username:"
                                textInput(name = "username")
                            }
                            p {
                                +"Password:"
                                passwordInput(name = "password")
                            }
                            p {
                                submitInput { value = "Resgister" }
                            }
                        }
                    }
                }
            }
            post {
                val params = call.receiveParameters()
                repository.createUser(params["username"]!!, params["password"]!!)

                call.respondRedirect("/login")
            }
        }
    }
}