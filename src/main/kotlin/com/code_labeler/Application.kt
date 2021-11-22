package com.code_labeler

import com.code_labeler.authentication.JwtConfig
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import com.code_labeler.plugins.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*

val jwtConfig = JwtConfig(System.getenv("JWT_SECRET"))

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
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
        DBFunctions.initDB()
    }.start(wait = true)
}
