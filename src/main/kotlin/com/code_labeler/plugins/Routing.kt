package com.code_labeler.plugins

import io.ktor.routing.routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.content.forEachPart
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentDisposition
import io.ktor.response.respondText
import io.ktor.response.header
import io.ktor.response.respondFile
import io.ktor.request.receiveMultipart
import java.io.File
import java.util.UUID

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        var fileDescription = ""
        var fileName = ""
        var uuid = ""
        post("/upload") {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        uuid = UUID.randomUUID().toString()
                        File("files/$uuid").writeBytes(fileBytes)
                    }
                }
            }

            call.respondText("file uploaded successfully to files/$uuid")
        }

        get("/download/{id}") {
            val id = call.parameters["id"]
            val file = File("files/$id")
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$id.csv")
                    .toString()
            )
            call.respondFile(file)
        }
    }

}
