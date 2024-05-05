package hu.bme.vik.templates

import hu.bme.vik.model.UserSession
import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTemplate(val userSession: UserSession?): Template<HTML> {
    val articleTitle = Placeholder<FlowContent>()
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        head {
            link(rel = "stylesheet", href = "/static/index.css", type = "text/css")
        }
        body {
            div(classes = "top-header") {
                div(classes = "header") {
                    a {
                        href = "/list"
                        p { +"List" }
                    }
                }
                div(classes = "header") {
                    a {
                        href = "/upload"
                        p { +"Upload" }
                    }
                }
                userSession?.let {
                    div(classes = "header") {
                        a {
                            href = "/logout"
                            p { +"Logout" }
                        }
                    }
                } ?: run {
                    div(classes = "header") {
                        a {
                            href = "/login"
                            p { +"Login" }
                        }
                    }
                    div(classes = "header") {
                        a {
                            href = "/register"
                            p { +"Register" }
                        }
                    }
                }
            }

            h1(classes = "title") {
                insert(articleTitle)
            }

            hr {  }

            insert(content)
        }
    }
}