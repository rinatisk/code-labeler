@file:Suppress("TooManyFunctions")

package com.code_labeler

import com.code_labeler.Files.name
import com.code_labeler.Files.owner
import com.code_labeler.Files.users
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import java.util.*

const val STANDARD_LENGTH = 100

object Users : LongIdTable() {
    val name = varchar("name", STANDARD_LENGTH)
    val encryptedPassword = varchar("encrypted_password", STANDARD_LENGTH)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(name)
}

object Files : Table() {
    val id = varchar("id", STANDARD_LENGTH)
    val name = varchar("name", STANDARD_LENGTH)
    val owner = long("owner")
    val users = varchar("users", STANDARD_LENGTH).nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object DBFunctions {
    init {
        val postgresProperties = Properties()
        postgresProperties.load(DBFunctions::class.java.classLoader.getResourceAsStream("postgres.properties"))
        Database.connect(
            url = postgresProperties.getProperty("url"),
            driver = postgresProperties.getProperty("driver"),
            user = postgresProperties.getProperty("user"),
            password = System.getenv("POSTGRES_PASSWORD")
        )
        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Files)
        }
    }

    fun isNewUser(username: String) = !transaction { Users.select { Users.name eq username }.count() > 0 }

    fun addUser(username: String, password: String) {
        transaction {
            Users.insert {
                it[name] = username
                it[encryptedPassword] = password
            }
        }
    }

    fun addFile(uuid: String, originalName: String, ownerId: Long) {
        transaction {
            Files.insert {
                it[id] = uuid
                it[name] = originalName
                it[owner] = ownerId
            }
        }
    }

    fun getUsers(uuid: String): List<Long> {
        val users = transaction { Files.select { Files.id eq uuid }.first()[users] }
        return Json.decodeFromString<List<Long>>(users ?: return emptyList())
    }

    fun getOwner(uuid: String) = transaction { Files.select { Files.id eq uuid }.first()[owner] }

    fun isOwner(uuid: String, idOfUser: Long) = getOwner(uuid) == idOfUser

    fun isUserAllowed(userId: Long, uuid: String): Boolean {
        return (userId in getUsers(uuid) || userId == getOwner(uuid))
    }

    fun allowUser(fileUuid: String, userId: Long) {
        val users = getUsers(fileUuid)
        if (userId !in users) {
            val newUsers = users.toMutableList()
            newUsers.add(userId)
            transaction {
                Files.update({ Files.id eq fileUuid }) {
                    it[Files.users] = Json.encodeToString(newUsers.toList())
                }
            }
        }
    }

    fun denyUser(fileUuid: String, userId: Long) {
        val users = getUsers(fileUuid)
        if (userId !in users) {
            val newUsers = users.toMutableList()
            newUsers.remove(userId)
            transaction {
                Files.update({ Files.id eq fileUuid }) {
                    it[Files.users] = Json.encodeToString(newUsers.toList())
                }
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

    fun getId(username: String) = transaction {
        Users.select { Users.name eq username }.first()[Users.id].value
    }
}
