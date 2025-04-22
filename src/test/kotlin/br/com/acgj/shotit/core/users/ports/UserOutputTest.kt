package br.com.acgj.shotit.core.users.ports

import br.com.acgj.shotit.core.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserOutputTest  {


    private lateinit var user: User


    @Test
    fun `should convert user to profile dto with all fields`() {
        user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123",
            profilePicture = "https://example.com/profile.jpg"
        )
        val dto = UserProfileDTO(user)

        assertThat(dto.name).isEqualTo(user.name)
        assertThat(dto.profilePicture).isEqualTo(user.profilePicture)
    }

    @Test
    fun `should throw null pointer exception when profile picture is null`() {
        val userWithoutPicture = User(
            username = "testuser2",
            name = "Test User 2",
            email = "test2@example.com",
            password = "password123"
        )

        assertThrows<NullPointerException> {
            UserProfileDTO(userWithoutPicture)
        }
    }
} 