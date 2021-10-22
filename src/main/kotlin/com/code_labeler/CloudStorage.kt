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
    private val bucketName = "code-labeler-bucket"

    suspend fun uploadJson(id: String, jsonFile: JsonFile) {
        val newObjectMetadata = mutableMapOf<String, String>()
        newObjectMetadata["original-name"] = jsonFile.originalName
        val byteStreamOfFile = ByteStream.fromBytes(jsonFile.jsonString.toByteArray())
        s3Client.putObject {
            bucket = bucketName
            key = id
            metadata = newObjectMetadata
            body = byteStreamOfFile
        }
    }

    suspend fun downloadJson(id: String): JsonFile {
        val getObjectRequest = GetObjectRequest {
            bucket = bucketName
            key = id
        }
        var originalName = ""
        var jsonString = ""
        s3Client.getObject(getObjectRequest) { getObjectResponse ->
            originalName = getObjectResponse.metadata?.get("original-name") ?: ""
            jsonString = getObjectResponse.body?.decodeToString() ?: ""
        }
        return JsonFile(jsonString, originalName)
    }

    suspend fun deleteFile(id: String) {
        s3Client.deleteObject {
            bucket = bucketName
            key = id
        }
    }
}
