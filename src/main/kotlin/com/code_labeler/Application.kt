package com.code_labeler

import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import com.code_labeler.plugins.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        configureRouting()
        initDB()
    }.start(wait = true)
}
