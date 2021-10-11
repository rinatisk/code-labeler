package com.code_labeler

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import kotlinx.serialization.Serializable

@Serializable
open class Label(val name: String) {
    override fun toString() = name
}

@Serializable
class CodeWithLabel(val code: String) {
    private var _label = Label("")
    val label: Label
        get() = _label

    constructor(code: String, label: Label) : this(code) {
        _label = label
    }

    fun changeLabel(newLabel: Label) {
        _label = newLabel
    }
}

@Serializable
class NewLabel(val numberOfSnippet: Int, val label: Label)

fun parseCsvString(string: String): List<CodeWithLabel> {
    val map = csvReader().readAllWithHeader(string)
    return map.map { CodeWithLabel(it.getOrDefault("code", ""), Label(name = it.getOrDefault("value", ""))) }
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
