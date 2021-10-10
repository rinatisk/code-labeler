package com.code_labeler.plugins

import com.code_labeler.CodeWithLabel
import com.code_labeler.NewLabel
import com.code_labeler.marshalCsvFile
import com.code_labeler.parseCsvString
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.*
import io.ktor.http.content.forEachPart
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
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
                        val list = parseCsvString(String(part.streamProvider().readBytes()))
                        val string = Json.encodeToString(list)
                        uuid = UUID.randomUUID().toString()
                        File("files/$uuid").writeText(string)
                    }
                }
            }

            call.respondText("file uploaded successfully to files/$uuid")
        }

        get("/files/{id}") {
            val id = call.parameters["id"]
            val file = File("files/$id")
            val temporaryFile = File("temporaryFiles/$id.csv")
            val list: List<CodeWithLabel> = Json.decodeFromString(file.readText())
            marshalCsvFile(list, temporaryFile)
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$id.csv")
                    .toString()
            )
            call.respondFile(temporaryFile)
            temporaryFile.delete()
        }

        delete("files/{id}") {
            val id = call.parameters["id"]
            File("files/$id").delete()
            call.respond(HttpStatusCode.OK, "OK")
        }

        put("files/{id}") {
            val id = call.parameters["id"]
            val newLabel = call.receive<NewLabel>()
            val file = File("files/$id")
            val list: List<CodeWithLabel> = Json.decodeFromString(file.readText())
            list[newLabel.numberOfSnippet].changeLabel(newLabel.label)
            file.writeText(Json.encodeToString(list))
            call.respond(HttpStatusCode.OK, "OK")
        }
    }
}
