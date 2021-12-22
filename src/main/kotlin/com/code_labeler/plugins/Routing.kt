@file:Suppress("SwallowedException")

package com.code_labeler.plugins

import com.code_labeler.*
import com.code_labeler.DBFunctions.isNewUser
import com.code_labeler.DBFunctions.isOwner
import com.code_labeler.authentication.JwtConfig
import com.code_labeler.authentication.LoginBody
import com.code_labeler.jwtConfig
import io.ktor.routing.*
import io.ktor.routing.post
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

fun Route.signup() {
    /**
     * Handles user registration, receives LoginBody
     */
    post("/signup") {
        val loginBody = call.receive<LoginBody>()
        if (isNewUser(loginBody.username)) {
            DBFunctions.addUser(loginBody.username, encrypt(loginBody.password))
            call.respond(HttpStatusCode.OK, "User registered successfully")
        } else {
            call.respond(HttpStatusCode.Conflict, "User already exists")
        }
    }
}

fun Route.login() {
    /**
     * Handles user authentication, receives LoginBody, sends JWT Token
     */
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
}

fun Route.fileUpload() {
    var uuid = ""
    authenticate {
        /**
         * Uploads a file to the server, gets the file itself and the owner token
         */
        post("/upload") {
            val user = call.authentication.principal as JwtConfig.JwtUser
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val string = Json.encodeToString(parseCsvString(String(part.streamProvider().readBytes())))
                        uuid = UUID.randomUUID().toString()
                        DBFunctions.addFile(uuid, part.originalFileName ?: "file.csv", user.userId)
                        CloudStorage.uploadJson(uuid, string)
                    }
                }
            }
            call.respondText("File uploaded successfully to files/$uuid")
        }
    }
}

fun Route.fileDownload() {
    authenticate {
        /**
         * Sends a file to the user, gets the uuid of a file and the token of the user who has access to this file
         */
        get("/files/{id}") {
            val user = call.authentication.principal as JwtConfig.JwtUser
            val fileUuid = call.parameters["id"] ?: ""
            if (DBFunctions.exists(fileUuid)) {
                if (DBFunctions.isUserAllowed(user.userId, fileUuid)) {
                    val originalName = DBFunctions.getOriginalName(fileUuid)
                    val jsonString = CloudStorage.downloadJson(fileUuid)
                    val temporaryFile = File(originalName)
                    val listOfSnippets: List<CodeWithLabel> = Json.decodeFromString(jsonString)
                    marshalCsvFile(listOfSnippets, temporaryFile)
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            originalName
                        ).toString()
                    )
                    call.respondFile(temporaryFile)
                    temporaryFile.delete()
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User doesn't have access to this file")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}

fun Route.addUser() {
    authenticate {
        /**
         * Gives the user access to the file.
         * For this he gets the uuid of the file,
         * the token of the owner and the name of the user who needs to be given access
         */
        put("files/{id}/add") {
            val userToAdd = call.receive<String>()
            val owner = call.authentication.principal as JwtConfig.JwtUser
            val fileUuid = call.parameters["id"] ?: ""
            if (DBFunctions.exists(fileUuid)) {
                if (isOwner(fileUuid, owner.userId)) {
                    DBFunctions.allowUser(fileUuid, DBFunctions.getId(userToAdd))
                    call.respond(HttpStatusCode.OK, "User added successfully")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User doesn't have access to this file")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}

fun Route.removeUser() {
    authenticate {
        /**
         * Takes away access to the file from the user.
         * To do this, it receives the uuid of the file,
         * the token of the owner  and the username from which access should be taken away
         */
        put("files/{id}/remove") {
            val userToRemove = call.receive<String>()
            val owner = call.authentication.principal as JwtConfig.JwtUser
            val fileUuid = call.parameters["id"] ?: ""
            if (DBFunctions.exists(fileUuid)) {
                if (isOwner(fileUuid, owner.userId)) {
                    DBFunctions.denyUser(fileUuid, DBFunctions.getId(userToRemove))
                    call.respond(HttpStatusCode.OK, "User removed successfully")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User doesn't have access to this file")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}

fun Route.deleteFile() {
    authenticate {
        /**
         * Deletes the file, for this it gets the uuid of the file and the token of the owner
         */
        delete("files/{id}") {
            val user = call.authentication.principal as JwtConfig.JwtUser
            val id = call.parameters["id"] ?: ""
            if (DBFunctions.exists(id)) {
                if (isOwner(id, user.userId)) {
                    CloudStorage.deleteFile(id)
                    DBFunctions.removeFile(id)
                    call.respond(HttpStatusCode.OK, "OK")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User doesn't have access to this file")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}

fun Route.modifyFile() {
    authenticate {
        /**
         * Modifies the file, for this it gets the uuid of the file,
         * the token of the user with access to the file and json with changes
         */
        put("files/{id}") {
            val user = call.authentication.principal as JwtConfig.JwtUser
            val id = call.parameters["id"] ?: ""
            val newLabel = call.receive<NewLabel>()
            if (DBFunctions.exists(id)) {
                if (DBFunctions.isUserAllowed(user.userId, id)) {
                    val jsonString = CloudStorage.downloadJson(id)
                    CloudStorage.uploadJson(id, changeLabel(jsonString, newLabel))
                    call.respond(HttpStatusCode.OK, "OK")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User doesn't have access to this file")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Such file does not exist")
            }
        }
    }
}

fun Application.configureRouting() {
    routing {
        signup()
        login()
        fileUpload()
        fileDownload()
        addUser()
        removeUser()
        deleteFile()
        modifyFile()
    }
}
