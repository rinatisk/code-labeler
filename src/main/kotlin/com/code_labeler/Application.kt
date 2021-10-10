package com.code_labeler

import com.code_labeler.authentication.JwtConfig
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import io.ktor.auth.*
import com.code_labeler.plugins.*
import io.ktor.application.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.gson

val jwtConfig = JwtConfig(System.getenv("JWT_SECRET"))

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(Authentication) {
            jwt {
                jwtConfig.configureKtorFeature(this)
            }
        }
        configureRouting()
    }.start(wait = true)
}
