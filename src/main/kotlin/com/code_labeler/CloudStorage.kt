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

    /**
     * Uploads a serialized file to cloud
     * @param uuid unique id of file, used as a key
     * @param jsonString serialized file
     */
    suspend fun uploadJson(uuid: String, jsonString: String) {
        val byteStreamOfFile = ByteStream.fromBytes(jsonString.toByteArray())
        s3Client.putObject {
            bucket = bucketName
            key = uuid
            body = byteStreamOfFile
        }
    }

    /**
     * Downloads serialized file and return
     * @param uuid unique id of file, used as a key
     */
    suspend fun downloadJson(uuid: String): String {
        val getObjectRequest = GetObjectRequest {
            bucket = bucketName
            key = uuid
        }
        var jsonString = ""
        s3Client.getObject(getObjectRequest) { getObjectResponse ->
            jsonString = getObjectResponse.body?.decodeToString() ?: ""
        }
        return jsonString
    }

    /**
     * Removes a file from the cloud
     * @param uuid unique id of file, used as a key
     */
    suspend fun deleteFile(uuid: String) {
        s3Client.deleteObject {
            bucket = bucketName
            key = uuid
        }
    }
}
