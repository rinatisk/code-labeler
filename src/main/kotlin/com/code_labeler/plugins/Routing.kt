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
import java.io.File
import java.util.*
import aws.sdk.kotlin.services.s3.model.NoSuchKey

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
                        CloudStorage.uploadJson(uuid, JsonFile(string, part.originalFileName ?: "file"))
                    }
                }
            }
            call.respondText("file uploaded successfully to files/$uuid")
        }

        get("/files/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                try {
                    val jsonFile = CloudStorage.downloadJson(id)
                    val temporaryFile = File("temporaryFiles/${jsonFile.originalName}.csv")
                    val listOfSnippets: List<CodeWithLabel> = Json.decodeFromString(jsonFile.jsonString)
                    marshalCsvFile(listOfSnippets, temporaryFile)
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "${jsonFile.originalName}.csv"
                        )
                            .toString()
                    )
                    call.respondFile(temporaryFile)
                    temporaryFile.delete()
                } catch (e: NoSuchKey) {
                    // maybe another exception is needed here
                    call.respond(HttpStatusCode.NotFound, "Such file does not exist")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        delete("files/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                CloudStorage.deleteFile(id)
                call.respond(HttpStatusCode.OK, "OK")
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }

        put("files/{id}") {
            val id = call.parameters["id"]
            // Normal responses need to be added here
            val newLabel = call.receive<NewLabel>()
            if (id != null) {
                try {

                    val jsonFile = CloudStorage.downloadJson(id)
                    jsonFile.changeString(changeLabel(jsonFile.jsonString, newLabel))
                    CloudStorage.uploadJson(id, jsonFile)
                    call.respond(HttpStatusCode.OK, "OK")
                } catch (e: NoSuchKey) {
                    call.respond(HttpStatusCode.NotFound, "Such file does not exist")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}
