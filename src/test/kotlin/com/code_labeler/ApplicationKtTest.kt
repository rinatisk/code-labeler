package com.code_labeler

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

internal class ApplicationKtTest {
    @Test
    fun parse() {
        val file = this.javaClass.getResource("simpleFile.csv").file
        val actual = parseCsvFile(File(file))

        assertEquals("example1 { }", actual.first().code)
    }

}
