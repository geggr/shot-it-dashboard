package br.com.acgj.shotit.core.users.controllers

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.auth.gateways.S3AvatarUploadGateway
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.UserRepository
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApplicationUser::class, InfraContainersForTestConfiguration::class)
class UserProfileControllerTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var applicationUser: ApplicationUser

    @MockBean
    private lateinit var gateway: S3AvatarUploadGateway

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should return 403 when user is not authenticated`() {
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return user profile when user is authenticated`() {
        val user = User(
            id = 1,
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123",
            profilePicture = "https://example.com/avatar.jpg"
        )
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)

        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Test User"))
            .andExpect(jsonPath("$.profilePicture").value("https://example.com/avatar.jpg"))
    }

    @Test
    fun `should return 403 when user is not logged`() {
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().`is`(403))
    }
} 