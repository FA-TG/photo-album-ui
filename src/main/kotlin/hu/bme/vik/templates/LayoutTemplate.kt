package hu.bme.vik.templates

import hu.bme.vik.Config
import io.ktor.server.html.*
import kotlinx.html.*

class LayoutTemplate(private val repetition: Int): Template<HTML> {
    override fun HTML.apply() {
        head {
            title = Config.text
            link(rel = "stylesheet", href = "style/index.css", type = "text/css")
        }
        body {
            h1 {
                +"Hello from ${Config.text}!"
            }
            (0 until repetition).forEach {
                h1 {
                    +"Hello from $it!"
                }
            }
        }
    }
}