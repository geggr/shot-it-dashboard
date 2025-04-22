package br.com.acgj.shotit.core.videosTags.controllers

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Import(InfraContainersForTestConfiguration::class)
class VideoTagControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tagRepository: VideoCategoryTagRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        tagRepository.deleteAll()
        SecurityContextHolder.clearContext()
        val user = userRepository.save(
            User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password"
        )
        )
        authenticated(user)
    }

    @Test
    @Transactional
    fun `should return empty list when no tags exist`() {
        mockMvc.perform(get("/api/tags"))
            .andExpect(status().isOk)
            .andExpect { result ->
                val content = result.response.contentAsString
                assertThat(content).isEqualTo("[]")
            }
    }

    @Test
    @Transactional
    fun `should return all tags when they exist`() {
        val tags = listOf(
            VideoCategoryTag(name = "kotlin"),
            VideoCategoryTag(name = "java"),
            VideoCategoryTag(name = "spring")
        )
        tagRepository.saveAll(tags)

        mockMvc.perform(get("/api/tags"))
            .andExpect(status().isOk)
            .andExpect { result ->
                val content = result.response.contentAsString
                assertThat(content).contains("\"name\":\"kotlin\"")
                assertThat(content).contains("\"name\":\"java\"")
                assertThat(content).contains("\"name\":\"spring\"")
            }
    }

    fun authenticated(user: User) {
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)
    }
} 