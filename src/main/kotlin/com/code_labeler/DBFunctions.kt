@file:Suppress("MatchingDeclarationName")

package com.code_labeler

import com.code_labeler.Files.name
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = integer("user_id").autoIncrement()
    val name = varchar("name", 20)
    val encryptedPassword = varchar("encrypted_password", 20)
    val token = varchar("token", 20).nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object Files : Table() {
    val id = varchar("id", 40)
    val name = varchar("name", 20)
    val allowedUsers = varchar("allowed-users", 20).nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object DBFunctions {

    fun initDB() {
        Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = ""
        )
        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Files)
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

    fun addFile(uuid: String, originalName: String) {
        transaction {
            Files.insert {
                it[id] = uuid
                it[name] = originalName
            }
        }
    }

    fun exists(uuid: String) = transaction { Files.select { Files.id eq uuid }.count() > 0 }

    fun removeFile(uuid: String) {
        transaction { Files.deleteWhere { Files.id eq uuid } }
    }

    fun getOriginalName(uuid: String): String {
        return transaction { Files.select { Files.id eq uuid }.first()[name] }
    }
}
