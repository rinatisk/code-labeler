package com.code_labeler.repository

import com.code_labeler.authentication.JwtConfig

/**
 * Basic interface for user data
 */
interface UserRepository {

    fun getUser(username: String, password: String): JwtConfig.User?
}
