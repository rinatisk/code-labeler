package com.code_labeler.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import kotlinx.serialization.Serializable

class JwtConfig(jwtSecret: String) {

    companion object Constants {
        // claims
        private const val CLAIM_USERID = "userId"
        private const val CLAIM_USERNAME = "userName"
    }

    private val config = HoconApplicationConfig(ConfigFactory.load())
    private val jwtIssuer = config.property("jwt.issuer").getString()
    private val jwtRealm = config.property("jwt.realm").getString()
    private val jwtAlgorithm = Algorithm.HMAC512(jwtSecret)
    private val jwtVerifier: JWTVerifier = JWT
        .require(jwtAlgorithm)
        .withIssuer(jwtIssuer)
        .build()

    /**
     * Generate a token for a authenticated user
     */
    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(jwtIssuer)
        .withClaim(CLAIM_USERID, user.userId)
        .withClaim(CLAIM_USERNAME, user.userName)
        .sign(jwtAlgorithm)

    /**
     * Configure the jwt ktor authentication feature
     */
    fun configureKtorFeature(config: JWTAuthenticationProvider.Configuration) = with(config) {
        verifier(jwtVerifier)
        realm = jwtRealm
        validate {
            val userId = it.payload.getClaim(CLAIM_USERID).asLong()

            val userName = it.payload.getClaim(CLAIM_USERNAME).asString()

            if (userId != null && userName != null) {
                User(userId, userName)
            } else {
                null
            }
        }
    }

    /**
     * POKO, that contains information of an authenticated user that is authenticated via jwt
     */
    @Serializable
    data class User(val userId: Long, val userName: String): Principal
}
