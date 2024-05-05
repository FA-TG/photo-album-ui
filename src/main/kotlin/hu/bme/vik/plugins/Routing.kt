package hu.bme.vik.plugins

import hu.bme.vik.model.UserSession
import hu.bme.vik.repository.PictureRepository
import hu.bme.vik.templates.LayoutTemplate
import hu.bme.vik.utils.format
import hu.bme.vik.utils.toBufferedImage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val repository by inject<PictureRepository>()

    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respondRedirect("/list")
        }
        get("/list") {
            if (call.parameters["orderBy"] != "name" && call.parameters["orderBy"] != "date") {
                call.respondRedirect("/list?orderBy=name")
            }

            var posts = repository.getAllPicture()

            if (call.parameters["orderBy"] == "name") {
                posts = posts.sortedBy { it.name }
            }

            if (call.parameters["orderBy"] == "date") {
                posts = posts.sortedBy { it.date }
            }

            call.respondHtmlTemplate(LayoutTemplate(call.sessions.get<UserSession>())) {
                articleTitle {
                    +"List"
                }
                content {
                    div(classes = "options") {
                        if (call.parameters["orderBy"] == "name") {
                            div(classes = "option") {
                                style = "display: inline-block;"
                                +"Order by name"
                            }
                        } else {
                            a(classes = "option") {
                                style = "display: inline-block;"
                                href = "/list?orderBy=name"
                                div {
                                    +"Order by name"
                                }
                            }
                        }

                        if (call.parameters["orderBy"] == "date") {
                            div(classes = "option") {
                                style = "display: inline-block;"
                                +"Order by date"
                            }
                        } else {
                            a(classes = "option") {
                                style = "display: inline-block;"
                                href = "/list?orderBy=date"
                                div {
                                    +"Order by date"
                                }
                            }
                        }
                    }

                    div(classes = "posts") {
                        posts.forEach { post ->
                            div(classes = "post") {
                                a {
                                    href = "/detail/${post.name}"
                                    div {
                                        p { +"Name: ${post.name}" }
                                    }
                                    div {
                                        p { +"Date: ${post.date.format()}" }
                                    }
                                    call.sessions.get<UserSession>()?.let {
                                        form(action = "/delete/${post.name}", method = FormMethod.post) {
                                            p {
                                                submitInput { value = "Delete" }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        get("/detail/{name}") {
            val name = call.parameters["name"]!!
            val post = repository.getByName(name)

            if (post == null) {
                call.respondRedirect("/list")
            }

            call.respondHtmlTemplate(LayoutTemplate(call.sessions.get<UserSession>())) {
                articleTitle {
                    +"Detail"
                }
                content {
                    div(classes = "single-post") {
                        div {
                            img {
                                src = "/images/${post!!.name}"
                                alt = "Post image"
                            }
                        }
                        div {
                            p { +"Name: ${post!!.name}" }
                        }
                        div {
                            p { +"Date: ${post!!.date.format()}" }
                        }
                    }
                }
            }
        }
        get("/images/{name}") {
            val name = call.parameters["name"]!!

            call.respondBytes(repository.getPicture(name))
        }
        authenticate("auth-form") {
            post("/delete/{name}") {
                val name = call.parameters["name"]!!

                repository.deletePicture(name)

                call.respondRedirect("/list")
            }
            route("upload") {
                get {
                    call.respondHtmlTemplate(LayoutTemplate(call.sessions.get<UserSession>())) {
                        articleTitle {
                            +"Upload"
                        }
                        content {
                            div(classes = "upload") {
                                form(
                                    action = "/upload",
                                    encType = FormEncType.multipartFormData,
                                    method = FormMethod.post
                                ) {
                                    p {
                                        +"Image:"
                                        fileInput {
                                            name = "image"
                                            id = "image"
                                            onChange = "previewImage(event)"
                                            accept = "image/*"
                                        }
                                    }
                                    p {
                                        submitInput { value = "Upload" }
                                    }
                                    div { id = "imagePreview" }
                                }
                            }
                            script(src = "/static/imagePreview.js") {  }
                        }
                    }
                }
                post {
                    var fileBytes: ByteArray? = null
                    var fileName: String? = null

                    call.receiveMultipart().forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                fileBytes = part.streamProvider().readBytes()
                                fileName = part.originalFileName
                            }
                            is PartData.FormItem -> {}
                            is PartData.BinaryItem -> {}
                            is PartData.BinaryChannelItem -> {}
                        }
                    }

                    if (fileBytes == null || fileName == null) {
                        call.respond(HttpStatusCode.BadRequest)
                    }

                    val bufferedImage = fileBytes!!.toBufferedImage()
                    repository.upload(bufferedImage, fileName!!)

                    call.respondRedirect("/list")
                }
            }
        }
    }
}