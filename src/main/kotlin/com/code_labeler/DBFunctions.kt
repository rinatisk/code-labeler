@file:Suppress("MatchingDeclarationName")

package com.code_labeler

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = integer("user_id").autoIncrement()
    val name = varchar("name", 10)
    val encryptedPassword = varchar("encrypted_password", 10)
    val token = varchar("token", 10).nullable()

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}

fun initDB() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "gsto"
    )
    transaction {
        SchemaUtils.create(Users)
    }
}

fun addUser(username: String, password: String) {
    transaction {
        Users.insert {
            it[name] = username
            it[encryptedPassword] = password
        }
    }
}
