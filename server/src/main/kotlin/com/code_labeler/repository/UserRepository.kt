package com.code_labeler.repository

/**
 * Basic interface for user data
 */
interface UserRepository {

    fun getUser(username: String, password: String): User?

    data class User(
        val userId: Long,
        val username: String
    )

}
