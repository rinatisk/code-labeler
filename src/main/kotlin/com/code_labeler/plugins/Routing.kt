package com.code_labeler.plugins

import com.code_labeler.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File
import java.util.UUID

fun Application.configureRouting() {
    routing {
        var fileDescription = ""
        val uuid = UUID.randomUUID().toString()
        post("/upload") {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }
                    is PartData.FileItem -> {
                        val string = Json.encodeToString(parseCsvString(String(part.streamProvider().readBytes())))
                        File("files/$uuid").writeText(string)
                    }
                }
            }
            call.respondText("file uploaded successfully to files/$uuid")
        }

        get("/files/{id}") {
            val id = call.parameters["id"]
            val file = File("files/$id")
            if (file.exists()) {
                val temporaryFile = File("temporaryFiles/$id.csv")
                getCsvFromJson(file, temporaryFile)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$id.csv")
                        .toString()
                )
                call.respondFile(temporaryFile)
                temporaryFile.delete()
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        delete("files/{id}") {
            val id = call.parameters["id"]
            val fileToDelete = File("files/$id")
            if (fileToDelete.exists()) {
                fileToDelete.delete()
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        put("files/{id}") {
            val id = call.parameters["id"]
            // Normal responses need to be added here
            val newLabel = call.receive<NewLabel>()
            val file = File("files/$id")
            if (file.exists()) {
                changeLabel(file, newLabel)
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}
