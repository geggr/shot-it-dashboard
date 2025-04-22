package br.com.acgj.shotit.core.auth.ports

import br.com.acgj.shotit.core.domain.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class SignUpRequestTest {

    @Test
    fun `should encrypt password when converting to user`() {
        val password = "testPassword123"
        val encoder = BCryptPasswordEncoder()
        val request = SignUpRequest(
            name = "Test User",
            username = "testuser",
            email = "test@example.com",
            password = password,
            picture = MockMultipartFile(
                "picture",
                "test.jpg",
                "image/jpeg",
                "test image content".toByteArray()
            )
        )

        val user = request.toUser(encoder)

        assertNotNull(user)
        assertTrue(encoder.matches(password, user.password))
        assertEquals("Test User", user.name)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
    }
} 