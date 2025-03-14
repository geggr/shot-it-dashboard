package br.com.acgj.shotit.core.auth.services

import br.com.acgj.shotit.core.domain.BadRequestError
import br.com.acgj.shotit.core.domain.UserRepository
import br.com.acgj.shotit.core.infra.auth.JWTService
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import br.com.acgj.shotit.core.auth.gateways.S3AvatarUploadGateway
import br.com.acgj.shotit.core.auth.ports.SignInRequest
import br.com.acgj.shotit.core.auth.ports.SignUpRequest
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val manager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val uploadService: S3AvatarUploadGateway,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JWTService
) {

    @Transactional
    fun register(request: SignUpRequest){
        val user = request.toUser(passwordEncoder)
        val url = runBlocking { uploadService.upload(request.username, request.picture) }

        user.profilePicture = url

        userRepository.save(user)
    }

    fun authenticate(request: SignInRequest): String {
        val token = UsernamePasswordAuthenticationToken(request.email, request.password)
        try {
            val authentication = manager.authenticate(token)
            val principal = authentication.principal as UserDetailsImpl

            return jwtService.generateToken(principal.user)
        }
        catch (exception: AuthenticationException){
            throw BadRequestError("Authentication Error", "authentication_error", "Invalid Credentials")
        }
    }

}