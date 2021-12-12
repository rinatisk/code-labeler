@file:Suppress("SwallowedException", "LongMethod")

package com.code_labeler.plugins

import com.code_labeler.*
import com.code_labeler.authentication.JwtConfig
import com.code_labeler.authentication.LoginBody
import com.code_labeler.jwtConfig
import com.code_labeler.repository.InMemoryUserRepository
import com.code_labeler.repository.UserRepository
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.http.content.*
import io.ktor.request.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.code_labeler.authentication.UserDB
import java.io.File
import java.util.*

fun Application.configureRouting() {
    routing {
        var uuid = ""
        post("/upload") {
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
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

        val userRepository: UserRepository = InMemoryUserRepository()

        post("/login") {
            val loginBody = call.receive<LoginBody>()

            val user = userRepository.getUser(loginBody.username, loginBody.password)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials!")
                return@post
            }

            val token = jwtConfig.generateToken(JwtConfig.JwtUser(user.userId, user.username))
            call.respond(token)
        }

        /**
         * methods in this field will be only executed if user is authorized
         */
        authenticate {
            get("/me") {
                val user = call.authentication.principal as JwtConfig.JwtUser
                call.respond(user)
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

        post("/login") {
            val loginBody = call.receive<LoginBody>()

            val user = UserDB.findUser(loginBody)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials!")
                return@post
            }

            val token = jwtConfig.generateToken(JwtConfig.JwtUser(user.id.value, user.username))
            call.respond(token)
        }

        /**
         * methods in this field will be only executed if user is authorized
         */
        authenticate {
            get("/me") {
                val user = call.authentication.principal as JwtConfig.JwtUser
                call.respond(user)
            }
        }
    }
}
