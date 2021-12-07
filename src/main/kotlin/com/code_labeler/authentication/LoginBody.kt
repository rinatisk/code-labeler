package com.code_labeler.authentication

import kotlinx.serialization.Serializable

@Serializable
data class LoginBody(
    val username: String,
    val password: String
)
