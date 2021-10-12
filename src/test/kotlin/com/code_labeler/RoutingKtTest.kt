package com.code_labeler

import com.code_labeler.plugins.configureRouting
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import java.io.File
import kotlin.test.*

internal class RoutingKtTest {
    @Test
    fun testRequests() = withTestApplication(Application::configureRouting) {
        with(handleRequest(HttpMethod.Post, "/upload") {
            val boundary = "WebAppBoundary"
            val resource = this@RoutingKtTest.javaClass.getResource("requestsTest/toUploadFile.csv")
            val fileBytes = File(resource.file).readBytes()

            addHeader(
                HttpHeaders.ContentType,
                ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
            )
            setBody(boundary, listOf(
                PartData.FormItem(
                    "fileName", { }, headersOf(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Inline
                            .withParameter(ContentDisposition.Parameters.Name, "description")
                            .toString()
                    )
                ),
                PartData.FileItem({ fileBytes.inputStream().asInput() }, {}, headersOf(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.File
                        .withParameter(ContentDisposition.Parameters.Name, "file")
                        .withParameter(ContentDisposition.Parameters.FileName, "simpleFile.csv")
                        .toString()
                )
                )
            )
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }
}