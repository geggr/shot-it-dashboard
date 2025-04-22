package br.com.acgj.shotit.core.auth.services

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.createBucket
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.auth.gateways.S3AvatarUploadGateway
import br.com.acgj.shotit.core.auth.ports.SignInRequest
import br.com.acgj.shotit.core.auth.ports.SignUpRequest
import br.com.acgj.shotit.core.domain.BadRequestError
import br.com.acgj.shotit.core.domain.UserRepository
import br.com.acgj.shotit.core.infra.auth.JWTService
import br.com.acgj.shotit.core.infra.cloud.AwsConfiguration
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.testcontainers.containers.localstack.LocalStackContainer

@SpringBootTest
@Import(
    InfraContainersForTestConfiguration::class,
    AwsConfiguration::class,
    JWTService::class
)
@AutoConfigureMockMvc
class AuthenticationServiceTest : LocalstackTestContainerConfiguration() {

    private lateinit var s3Client: S3Client
    private lateinit var uploadGateway: S3AvatarUploadGateway
    private lateinit var authenticationService: AuthenticationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtService: JWTService

    @Autowired
    private lateinit var manager: AuthenticationManager

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        
        s3Client = S3Client {
            endpointUrl = Url.parse(localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString())
            region = localstack.region
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = localstack.accessKey,
                    secretAccessKey = localstack.secretKey
                )
            )
        }

        uploadGateway = S3AvatarUploadGateway(s3Client)
        authenticationService = AuthenticationService(manager, userRepository, uploadGateway, passwordEncoder, jwtService)

        runBlocking {
            try {
                s3Client.createBucket {
                    bucket = "shotit"
                }
            } catch (e: Exception) {
                // Bucket might already exist
            }
        }
    }

    @Test
    fun `should create user with profile picture when registering`() {
        val request = SignUpRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            name = "testuser",
            picture = MockMultipartFile("test.jpg", "files".toByteArray())
        )

        authenticationService.register(request)

        val savedUser = userRepository.findByEmail(request.email).get()
        assertThat(savedUser.username).isEqualTo(request.username)
        assertThat(savedUser.email).isEqualTo(request.email)
        assertThat(savedUser.name).isEqualTo(request.name)
        assertThat(passwordEncoder.matches(request.password, savedUser.password)).isTrue()
        assertThat(savedUser.profilePicture).isNotNull()
    }

    @Test
    fun `should return JWT token when authenticating with valid credentials`() {
        val user = SignUpRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            name = "testuser",
            picture = MockMultipartFile("test.jpg", "files".toByteArray())
        )
        authenticationService.register(user)

        val request = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )

        val token = authenticationService.authenticate(request)

        assertThat(token).isNotNull()
        assertThat(jwtService.extractUserFromToken(token)).isEqualTo(user.email)
    }

    @Test
    fun `should throw BadRequestError when authenticating with invalid credentials`() {
        val request = SignInRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        assertThrows<BadRequestError> {
            authenticationService.authenticate(request)
        }
    }
} 