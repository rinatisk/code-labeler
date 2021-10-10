package com.code_labeler.plugins

import com.code_labeler.authentication.JwtConfig
import com.code_labeler.entities.LoginBody
import com.code_labeler.jwtConfig
import com.code_labeler.repository.InMemoryUserRepository
import com.code_labeler.repository.UserRepository
import io.ktor.routing.routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.content.forEachPart
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.request.receive
import io.ktor.response.respond
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
    }

}
