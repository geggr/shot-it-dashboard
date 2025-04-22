package br.com.acgj.shotit.core.infra.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthenticationFilter(
    private val service: UserDetailsService,
    private val jwtService: JWTService
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val AUTHORIZATION_PREFIX = "Bearer "
    }

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {

        request.getHeader(AUTHORIZATION_HEADER)
            ?.run(::extractTokenFromHeader)
            ?.run(::extractUserFromToken)
            ?.let(::authenticate)

        chain.doFilter(request, response)
    }

    fun authenticate(email: String){
        val user = service.loadUserByUsername(email)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
    }

    fun extractUserFromToken(token: String) = jwtService.extractUserFromToken(token)

    fun extractTokenFromHeader(value: String) = value
            .takeIf { it.startsWith(AUTHORIZATION_PREFIX)}
            ?.substring(AUTHORIZATION_PREFIX.length)
}