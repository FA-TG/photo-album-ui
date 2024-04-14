package hu.bme.vik.templates

import hu.bme.vik.model.UserSession
import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTemplate(val userSession: UserSession?): Template<HTML> {
    val articleTitle = Placeholder<FlowContent>()
    val content = Placeholder<FlowContent>()
    override fun HTML.apply() {
        head {
            link(rel = "stylesheet", href = "style/index.css", type = "text/css")
        }
        body {
            h1 {
                insert(articleTitle)
            }
            div {
                a {
                    href = "/list"
                    p { +"List" }
                }
            }
            div {
                a {
                    href = "/upload"
                    p { +"Upload" }
                }
            }
            userSession?.let {
                div {
                    a {
                        href = "/logout"
                        p { +"Logout" }
                    }
                }
            } ?: run {
                div {
                    a {
                        href = "/login"
                        p { +"Login" }
                    }
                }
                div {
                    a {
                        href = "/register"
                        p { +"Register" }
                    }
                }
            }

            hr {  }

            insert(content)
        }
    }
}