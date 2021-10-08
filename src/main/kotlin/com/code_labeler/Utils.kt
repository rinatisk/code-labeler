package com.code_labeler

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

open class Label(val name: String) {
    override fun toString() = name
}

class CodeWithLabel(val code: String, val label: Label) {
}

fun parseCsvFile(file: File): List<CodeWithLabel> {
    val map = csvReader().readAllWithHeader(file)
    return map.map { CodeWithLabel(it.getOrDefault("code", ""), Label(name = it.getOrDefault("value", ""))) }
}

fun marshalCsvFile(listOfSnippets: List<CodeWithLabel>, file: File) {
    val title = listOf(listOf("code", "labels"))
    val rows = listOfSnippets.map { listOf(it.code, it.label.toString()) }
    val fullList = title + rows
    csvWriter().writeAll(fullList, file)
}

