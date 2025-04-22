package br.com.acgj.shotit.core.infra.auth

import br.com.acgj.shotit.core.domain.UnauthorizedError
import br.com.acgj.shotit.core.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.GrantedAuthority

class ApplicationUserTest {

    private val applicationUser = ApplicationUser()

    @Test
    fun `should return null when security context is empty`() {
        SecurityContextHolder.clearContext()
        
        val result = applicationUser.fromContext()
        
        assertThat(result).isNull()
    }

    @Test
    fun `should return null when authentication is not username password`() {
        val securityContext = SecurityContextImpl()
        securityContext.authentication = TestAuthentication()
        SecurityContextHolder.setContext(securityContext)
        
        val result = applicationUser.fromContext()
        
        assertThat(result).isNull()
    }

    @Test
    fun `should return user when authentication is valid`() {
        val user = User(
            id = 1,
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)
        
        val result = applicationUser.fromContext()
        
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `should throw unauthorized error when context is empty and requiring authorization`() {
        SecurityContextHolder.clearContext()
        
        assertThatThrownBy { applicationUser.fromAuthorizedContext() }
            .isInstanceOf(UnauthorizedError::class.java)
    }

    @Test
    fun `should return user when context is valid and requiring authorization`() {
        val user = User(
            id = 1,
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)
        
        val result = applicationUser.fromAuthorizedContext()
        
        assertThat(result).isEqualTo(user)
    }

    private class TestAuthentication : org.springframework.security.core.Authentication {
        override fun getName() = "test"
        override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()
        override fun getCredentials() = null
        override fun getDetails() = null
        override fun getPrincipal() = "test"
        override fun isAuthenticated() = true
        override fun setAuthenticated(isAuthenticated: Boolean) {}
    }
} 