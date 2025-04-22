package br.com.acgj.shotit.core.infra.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import jakarta.servlet.FilterChain
import br.com.acgj.shotit.core.domain.User
import org.springframework.security.core.userdetails.UserDetailsService

@ExtendWith(MockitoExtension::class)
class AuthenticationFilterTest {

    @Mock
    private lateinit var userDetailsService: UserDetailsService

    @Mock
    private lateinit var filterChain: FilterChain

    private lateinit var jwtService: JWTService
    private lateinit var filter: AuthenticationFilter
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setup() {
        jwtService = JWTService("your-test-secret-key-that-is-at-least-32-chars")
        filter = AuthenticationFilter(userDetailsService, jwtService)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should not authenticate when authorization header is missing`() {
        filter.doFilterInternal(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    fun `should not authenticate when authorization header does not start with Bearer`() {
        request.addHeader("Authorization", "Basic sometoken")

        filter.doFilterInternal(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    fun `should authenticate user when valid bearer token is provided`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "user@example.com",
            password = "password123"
        )
        val token = jwtService.generateToken(user)
        val userDetails = UserDetailsImpl(user)

        request.addHeader("Authorization", "Bearer $token")
        `when`(userDetailsService.loadUserByUsername(user.email)).thenReturn(userDetails)

        filter.doFilterInternal(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertThat(SecurityContextHolder.getContext().authentication).isNotNull
        assertThat(SecurityContextHolder.getContext().authentication.principal).isEqualTo(userDetails)
    }

    @Test
    fun `should extract token correctly from authorization header`() {
        val token = "mytoken123"
        val authHeader = "Bearer $token"

        val result = filter.extractTokenFromHeader(authHeader)

        assertThat(result).isEqualTo(token)
    }

    @Test
    fun `should return null when extracting token from invalid authorization header`() {
        val authHeader = "Basic mytoken123"

        val result = filter.extractTokenFromHeader(authHeader)

        assertThat(result).isNull()
    }

    @Test
    fun `should extract user from valid token`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "user@example.com",
            password = "password123"
        )
        val token = jwtService.generateToken(user)

        val result = filter.extractUserFromToken(token)

        assertThat(result).isEqualTo(user.email)
    }

    @Test
    fun `should return null when extracting user from invalid token`() {
        val result = filter.extractUserFromToken("invalid-token")

        assertThat(result).isNull()
    }

    @Test
    fun `should set authentication in security context when authenticating user`() {
        val email = "user@example.com"
        val user = User(
            username = "testuser",
            name = "Test User",
            email = email,
            password = "password123"
        )
        val userDetails = UserDetailsImpl(user)
        `when`(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails)

        filter.authenticate(email)

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNotNull
        assertThat(authentication.principal).isEqualTo(userDetails)
        assertThat(authentication.authorities).isEqualTo(userDetails.authorities)
    }
} 