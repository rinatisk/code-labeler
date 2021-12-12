package com.code_labeler

import com.code_labeler.authentication.JwtConfig
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import com.code_labeler.plugins.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.serialization.*

val jwtConfig = JwtConfig("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE2MzkzMzczNTN9._FgNjt6__JlksmOAfah91OpxsTmrkn8T_oj4CcWgB8E")

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

fun Application.main() {
    install(CORS) {
        anyHost()
    }
    configureRouting()
}
