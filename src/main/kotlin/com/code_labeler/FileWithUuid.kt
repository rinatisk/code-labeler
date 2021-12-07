package com.code_labeler

import java.io.File
import java.util.*

data class FileWithUuid(val uuid: UUID, val file: File)

fun createFile(): FileWithUuid {
    val uuid = UUID.randomUUID()
    return FileWithUuid(uuid, File("files/$uuid"))
}

fun addFile(fileWithUuid: FileWithUuid, string: String) {
    fileWithUuid.file.writeText(string)
}
