package com.code_labeler

import java.io.File
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path


internal class UtilsKtTest {
    @Test
    fun marshalTest1(@TempDir tempDir: Path) {
        val tempResource = tempDir.resolve("temp_out.csv").toFile()
        val list = listOf(CodeWithLabel("ada", Label("daad")), CodeWithLabel("d", Label("dd")))
        marshalCsvFile(list, tempResource)
        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile1.csv")
        assertEquals(expectedFile.readText(), tempResource.readText().replace("\r", ""));
    }

    @Test
    fun marshalTest2(@TempDir tempDir: Path) {
        val tempResource = tempDir.resolve("temp_out.csv").toFile()
        val list = listOf(
            CodeWithLabel("..\";;codeSnippet((((}}\"'\"}}}", Label("label1")),
            CodeWithLabel("__codeSnippet(\"\"::;;;;\"", Label("label2"))
        )
        marshalCsvFile(list, tempResource)
        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile2.csv")
        assertEquals(expectedFile.readText(), tempResource.readText().replace("\r", ""))

    }

    @Test
    fun marshalTest3(@TempDir tempDir: Path) {
        val tempResource = tempDir.resolve("temp_out.csv").toFile()
        val list = listOf(CodeWithLabel("code\nsnippet1", Label("label1")),
            CodeWithLabel("code snippet\n\n2", Label("label2")))
        marshalCsvFile(list, tempResource)

        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile3.csv")

        assertEquals(expectedFile.readText(), tempResource.readText().replace("\r", ""))
    }
}
