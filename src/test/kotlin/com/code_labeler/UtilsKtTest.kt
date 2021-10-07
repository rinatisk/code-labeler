package com.code_labeler

import org.junit.Test
import com.code_labeler.marshalCsvFile
import java.io.File
import kotlin.test.assertEquals


internal class UtilsKtTest {
   @Test
   fun marshal() {
       val file = this.javaClass.getResource("simple_out_file.csv").file

       val list = listOf(CodeWithLabel("ada", Label("daad")), CodeWithLabel("d", Label("dd")))
       marshalCsvFile(list, File(file))
       assertEquals("", File(file).readText())
   }
}