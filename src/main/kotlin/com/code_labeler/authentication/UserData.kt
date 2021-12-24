package com.code_labeler.authentication

import com.code_labeler.Users
import com.code_labeler.isPasswordCorrect
import io.ktor.auth.jwt.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class UserBody(
    val id: Long,
    val username: String
)

class User(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, User>(Users)

    val username by Users.name
    val password by Users.encryptedPassword
}

object UserDB {
    private fun getUsers(): List<User> = transaction { User.all().toList() }

    fun findUser(credential: LoginBody): User? =
        getUsers().firstOrNull {
            isPasswordCorrect(
                credential.password,
                it.password
            ) && it.username == credential.username
        }
}
