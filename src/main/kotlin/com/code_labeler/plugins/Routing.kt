package com.code_labeler.plugins

import com.code_labeler.authentication.JwtConfig
import com.code_labeler.entities.LoginBody
import com.code_labeler.jwtConfig
import com.code_labeler.repository.InMemoryUserRepository
import com.code_labeler.repository.UserRepository
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.request.*

fun Application.configureRouting() {
    // Starting point for a Ktor app:
    routing {
        get("/") {
            call.respondText("Hello World!")
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
