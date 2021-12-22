package com.code_labeler

import com.code_labeler.authentication.JwtConfig
import io.ktor.server.tomcat.*
import com.code_labeler.plugins.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.serialization.*

val jwtConfig = JwtConfig(System.getenv("JWT_SECRET"))

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
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
}
