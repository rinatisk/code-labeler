package com.code_labeler.repository

import com.code_labeler.authentication.JwtConfig

@SuppressWarnings("MagicNumber")

class InMemoryUserRepository : UserRepository {
    private val credentialsToUsers = mapOf<String, JwtConfig.User>(
        "admin:admin" to JwtConfig.User(1, "admin"),
        "rinat:1488" to JwtConfig.User(2, "rinat"),
        "goro:majima" to JwtConfig.User(3, "prophet")
    )

    override fun getUser(username: String, password: String): JwtConfig.User? {
        return credentialsToUsers["$username:$password"]
    }
}
