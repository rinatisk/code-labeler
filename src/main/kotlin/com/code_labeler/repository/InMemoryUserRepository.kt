package com.code_labeler.repository

class InMemoryUserRepository : UserRepository {
    private val credentialsToUsers = mapOf<String, UserRepository.User>(
        "admin:admin" to UserRepository.User(1, "admin"),
        "rinat:1488" to UserRepository.User(2, "rinat"),
        "goro:majima" to UserRepository.User(3, "prophet")
    )

    override fun getUser(username: String, password: String): UserRepository.User? {
        return credentialsToUsers["$username:$password"]
    }
}