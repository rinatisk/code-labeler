@file:Suppress("SwallowedException", "LongMethod")

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
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionScope
import java.io.File
import java.util.*

fun Application.configureRouting() {
    routing {
        var fileDescription = ""
        var uuid = ""
        post("/upload") {
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }
                    is PartData.FileItem -> {
                        val string = Json.encodeToString(parseCsvString(String(part.streamProvider().readBytes())))
                        uuid = UUID.randomUUID().toString()
                        DBFunctions.addFile(uuid, part.originalFileName ?: "file.csv")
                        CloudStorage.uploadJson(uuid, string)
                    }
                }
            }
            call.respondText("file uploaded successfully to files/$uuid")
        }

        get("/files/{id}") {
            val id = call.parameters["id"] ?: ""
            if (DBFunctions.exists(id)) {
                val originalName = DBFunctions.getOriginalName(id)
                val jsonString = CloudStorage.downloadJson(id)
                val temporaryFile = File(originalName)
                val listOfSnippets: List<CodeWithLabel> = Json.decodeFromString(jsonString)
                marshalCsvFile(listOfSnippets, temporaryFile)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        originalName
                    )
                        .toString()
                )
                call.respondFile(temporaryFile)
                temporaryFile.delete()
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        delete("files/{id}") {
            val id = call.parameters["id"] ?: ""
            if (DBFunctions.exists(id)) {
                CloudStorage.deleteFile(id)
                DBFunctions.removeFile(id)
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        put("files/{id}") {
            val id = call.parameters["id"] ?: ""
            // Normal responses need to be added here
            val newLabel = call.receive<NewLabel>()
            if (DBFunctions.exists(id)) {
                val jsonString = CloudStorage.downloadJson(id)
                CloudStorage.uploadJson(id, changeLabel(jsonString, newLabel))
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}
