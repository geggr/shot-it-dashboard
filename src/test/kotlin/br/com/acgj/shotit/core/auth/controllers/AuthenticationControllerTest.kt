package br.com.acgj.shotit.core.auth.controllers

import br.com.acgj.shotit.core.auth.ports.SignInRequest
import br.com.acgj.shotit.core.auth.ports.SignUpRequest
import br.com.acgj.shotit.core.auth.services.AuthenticationService
import br.com.acgj.shotit.core.domain.BadRequestError
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthenticationControllerTest {

    private lateinit var authenticationService: AuthenticationService
    private lateinit var authenticationController: AuthenticationController

    @BeforeEach
    fun setup() {
        authenticationService = mockk()
        authenticationController = AuthenticationController(authenticationService)
    }

    @Test
    fun `should successfully handle sign-in request`() {
        // Arrange
        val signInRequest = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )
        val expectedToken = "jwt.token.here"

        every { authenticationService.authenticate(signInRequest) } returns expectedToken

        // Act
        val response = authenticationController.handleSignIn(signInRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedToken, response.body?.token)
        verify { authenticationService.authenticate(signInRequest) }
    }

    @Test
    fun `should successfully handle sign-up request`() {
        // Arrange
        val picture = MockMultipartFile(
            "picture",
            "test.jpg",
            "image/jpeg",
            "test image content".toByteArray()
        )
        val signUpRequest = SignUpRequest(
            name = "Test User",
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            picture = picture
        )

        every { authenticationService.register(signUpRequest) } just runs

        // Act
        val response = authenticationController.handleSignUp(signUpRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify { authenticationService.register(signUpRequest) }
    }

    @Test
    fun `should handle authentication failure in sign-in`() {
        // Arrange
        val signInRequest = SignInRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        val expectedError = BadRequestError(
            "Authentication Error",
            "authentication_error",
            "Invalid Credentials"
        )

        every { authenticationService.authenticate(signInRequest) } throws expectedError

        // Act & Assert
        val exception = assertThrows<BadRequestError> {
            authenticationController.handleSignIn(signInRequest)
        }

        assertEquals(expectedError.message, exception.message)
        verify { authenticationService.authenticate(signInRequest) }
    }

    @Test
    fun `should handle empty sign-up request`() {
        // Arrange
        val picture = MockMultipartFile(
            "picture",
            "test.jpg",
            "image/jpeg",
            ByteArray(0)
        )
        val signUpRequest = SignUpRequest(
            name = "",
            username = "",
            email = "",
            password = "",
            picture = picture
        )

        every { authenticationService.register(signUpRequest) } just runs

        // Act
        val response = authenticationController.handleSignUp(signUpRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify { authenticationService.register(signUpRequest) }
    }

    @Test
    fun `should handle null response body in sign-in`() {
        // Arrange
        val signInRequest = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )
        val expectedToken = "jwt.token.here"

        every { authenticationService.authenticate(signInRequest) } returns expectedToken

        // Act
        val response = authenticationController.handleSignIn(signInRequest)

        // Assert
        assertNotNull(response.body)
        assertEquals(expectedToken, response.body?.token)
    }
} 