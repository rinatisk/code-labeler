package com.code_labeler

import aws.sdk.kotlin.runtime.auth.Credentials
import aws.sdk.kotlin.runtime.auth.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.decodeToString

object CloudStorage {
    private val awsCreds = Credentials(
        "",
        ""
    )
    private val s3Client = S3Client {
        region = "eu-central-1"
        credentialsProvider = StaticCredentialsProvider(awsCreds)
    }
    private const val bucketName = "code-labeler-bucket"

    suspend fun uploadJson(id: String, jsonString: String) {
        val byteStreamOfFile = ByteStream.fromBytes(jsonString.toByteArray())
        s3Client.putObject {
            bucket = bucketName
            key = id
            body = byteStreamOfFile
        }
    }

    suspend fun downloadJson(id: String): String {
        val getObjectRequest = GetObjectRequest {
            bucket = bucketName
            key = id
        }
        var jsonString = ""
        s3Client.getObject(getObjectRequest) { getObjectResponse ->
            jsonString = getObjectResponse.body?.decodeToString() ?: ""
        }
        return jsonString
    }

    suspend fun deleteFile(id: String) {
        s3Client.deleteObject {
            bucket = bucketName
            key = id
        }
    }
}
