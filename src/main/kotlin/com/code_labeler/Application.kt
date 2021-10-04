package com.code_labeler

import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import com.code_labeler.plugins.*

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}
