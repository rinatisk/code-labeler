package com.code_labeler

import org.junit.Test
import com.code_labeler.marshalCsvFile
import java.io.File
import kotlin.test.assertEquals


internal class UtilsKtTest {
    @Test
    fun marshalTest1() {
        val file = this.javaClass.getResource("simpleOutFile.csv").file
        val list = listOf(CodeWithLabel("ada", Label("daad")), CodeWithLabel("d", Label("dd")))
        marshalCsvFile(list, File(file))
        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile1.csv")
        assertEquals(expectedFile.readText(), File(file).readText());
    }

    @Test
    fun marshalTest2() {
        val file = this.javaClass.getResource("simpleOutFile.csv").file
        val list = listOf(
            CodeWithLabel("..\";;codeSnippet((((}}\"'\"}}}", Label("label1")),
            CodeWithLabel("__codeSnippet(\"\"::;;;;\"", Label("label2"))
        )
        marshalCsvFile(list, File(file))
        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile2.csv")
        assertEquals(expectedFile.readText(), File(file).readText())
    }

    @Test
    fun marshalTest3() {
        val file = this.javaClass.getResource("simpleOutFile.csv").file
        val list = listOf(CodeWithLabel("code\nsnippet1", Label("label1")), CodeWithLabel("code snippet\n\n2", Label("label2")))
        marshalCsvFile(list, File(file))
        val expectedFile = this.javaClass.getResource("marshalTest/expectedFile3.csv")
        assertEquals(expectedFile.readText().replace("\r", ""), File(file).readText().replace("\r", ""))
    }
}