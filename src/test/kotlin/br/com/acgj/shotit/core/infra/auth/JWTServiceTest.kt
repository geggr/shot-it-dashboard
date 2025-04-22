package br.com.acgj.shotit.core.infra.auth

import br.com.acgj.shotit.core.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JWTServiceTest {

    private val jwtService = JWTService("your-test-secret-key-that-is-at-least-32-chars")

    @Test
    fun `should generate token with user email`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val token = jwtService.generateToken(user)

        val extractedEmail = jwtService.extractUserFromToken(token)
        assertThat(extractedEmail).isEqualTo(user.email)
    }

    @Test
    fun `should return null when token is invalid`() {
        val result = jwtService.extractUserFromToken("invalid.token.here")

        assertThat(result).isNull()
    }

    @Test
    fun `should return null when token is empty`() {
        val result = jwtService.extractUserFromToken("")

        assertThat(result).isNull()
    }

    @Test
    fun `should return null when token is signed with different secret`() {
        val differentJwtService = JWTService("different-secret-key-that-is-at-least-32-chars")
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val token = differentJwtService.generateToken(user)
        val result = jwtService.extractUserFromToken(token)

        assertThat(result).isNull()
    }

    @Test
    fun `should generate different tokens for same user on multiple calls`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val token1 = jwtService.generateToken(user)
        Thread.sleep(1000)// waiting for the token to change
        val token2 = jwtService.generateToken(user)

        assertThat(token1).isNotEqualTo(token2)
        assertThat(jwtService.extractUserFromToken(token1)).isEqualTo(user.email)
        assertThat(jwtService.extractUserFromToken(token2)).isEqualTo(user.email)
    }
} 