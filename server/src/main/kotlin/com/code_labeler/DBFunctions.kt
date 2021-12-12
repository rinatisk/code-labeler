@file:Suppress("MatchingDeclarationName")

package com.code_labeler

import com.code_labeler.Files.name
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

const val STANDARD_LENGTH = 20
const val ID_LENGTH = 40

object Users : LongIdTable() {
    val name = varchar("name", STANDARD_LENGTH)
    val encryptedPassword = varchar("encrypted_password", STANDARD_LENGTH)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object Files : Table() {
    val id = varchar("id", ID_LENGTH)
    val name = varchar("name", STANDARD_LENGTH)
    val allowedUsers = varchar("allowed-users", STANDARD_LENGTH).nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object DBFunctions {

    fun initDB() {
        Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "rinat",
            password = "2002"
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
