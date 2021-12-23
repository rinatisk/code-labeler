@file:Suppress("TooManyFunctions")

package com.code_labeler

import com.code_labeler.Files.name
import com.code_labeler.Files.owner
import com.code_labeler.Files.users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import java.net.URI
import java.util.*

const val STANDARD_LENGTH = 100

/**
 * @property name - username
 * @property encryptedPassword - password hashed with BCrypt
 */
object Users : LongIdTable() {
    val name = varchar("name", STANDARD_LENGTH)
    val encryptedPassword = varchar("encrypted_password", STANDARD_LENGTH)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(name)
}

/**
 * @property id - uuid of the file
 * @property name - original name of the file
 * @property owner - id of the user who can delete the file, give or take away access from other users
 * @property users - list of users who can modify the file
 */
object Files : Table() {
    val id = varchar("id", STANDARD_LENGTH)
    val name = varchar("name", STANDARD_LENGTH)
    val owner = long("owner")
    val users = varchar("users", STANDARD_LENGTH).nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object DB {
    init {
        Database.connect(
            dataSource()
        )
        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.create(Files)
        }
    }

    private fun dataSource(): HikariDataSource {
        val config = HikariConfig()
        val dbUri = URI(System.getenv("DATABASE_URL"))
        val dbUserInfo = dbUri.userInfo
        config.username = dbUserInfo.split(":")[0]
        config.password = dbUserInfo.split(":")[1]
        config.jdbcUrl = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}"
        return HikariDataSource(config)
    }

    private fun getUsers(uuid: String): List<Long> {
        val users = transaction { Files.select { Files.id eq uuid }.first()[users] }
        return Json.decodeFromString(users ?: return emptyList())
    }

    private fun getOwner(uuid: String) = transaction { Files.select { Files.id eq uuid }.first()[owner] }

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

    fun isOwner(uuid: String, idOfUser: Long) = getOwner(uuid) == idOfUser

    fun isUserAllowed(userId: Long, uuid: String): Boolean {
        return (userId in getUsers(uuid) || userId == getOwner(uuid))
    }

    fun allowUser(fileUuid: String, userId: Long) {
        val users = getUsers(fileUuid)
        if (userId !in users) {
            val allowedUsers = users.toMutableList()
            allowedUsers.add(userId)
            transaction {
                Files.update({ Files.id eq fileUuid }) {
                    it[Files.users] = Json.encodeToString(allowedUsers.toList())
                }
            }
        }
    }

    fun denyUser(fileUuid: String, userId: Long) {
        val users = getUsers(fileUuid)
        if (userId in users) {
            val allowedUsers = users.toMutableList()
            allowedUsers.remove(userId)
            transaction {
                Files.update({ Files.id eq fileUuid }) {
                    it[Files.users] = Json.encodeToString(allowedUsers.toList())
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
