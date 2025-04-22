package br.com.acgj.shotit.core.videos.controllers

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import br.com.acgj.shotit.core.infra.cloud.AwsConfiguration
import br.com.acgj.shotit.core.videos.gateways.S3ThumbnailGateway
import br.com.acgj.shotit.core.videos.services.RetrieveVideoService
import jakarta.persistence.EntityManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.net.URL


@SpringBootTest
@AutoConfigureMockMvc
@Import(ApplicationUser::class, AwsConfiguration::class, InfraContainersForTestConfiguration::class )
class VideoControllerTest : LocalstackTestContainerConfiguration() {



    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var applicationUser: ApplicationUser

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var retrieveVideoService: RetrieveVideoService

    @Autowired
    private lateinit var thumbnailRepository: ThumbnailRepository

    @Autowired
    private lateinit var s3ThumbnailGateway: S3ThumbnailGateway

    @Autowired
    private lateinit var s3Client: S3Client

    private lateinit var user: User
    private lateinit var video: Video

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        user = userRepository.save(User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password"
        ))
        authenticated(user)

        video = videoRepository.save(Video(
            user = user,
            name = "Test Video",
            status = VideoStatus.SUCCESS,
        ))
        val thumbnail = thumbnailRepository.save(
            Thumbnail(
                video = video,
                url = "/principal.png",
                principal = true
            )
        )
        video.thumbnails = mutableListOf(thumbnail)
        videoRepository.save(video)
    }


    @Test
    fun `should return user videos when authenticated`() {
        mockMvc.perform(get("/api/videos"))
            .andExpectAll(
                status().isOk,
                jsonPath("$.[0].id").value(video.id),
                jsonPath("$.[0].name").value(video.name),
            )
    }

    @Test
    @Transactional
    fun `should return specific video when exists`() {
        mockMvc.perform(get("/api/videos/${video.id}"))
            .andExpectAll(
                status().isOk,
                jsonPath("$.id").value(video.id),
                jsonPath("$.name").value(video.name),
            )
    }

    @Test
    fun `should return not found when video does not exist`() {
        mockMvc.perform(get("/api/videos/9999"))
            .andExpectAll(
                status().isNotFound,
                jsonPath("$.message").value("Video not found"),
                jsonPath("$.errors").isEmpty()
            )
    }

    @Test
    @Transactional
    fun `should download thumbnails when video exists`() {
        runBlocking {
            val url = URL("https://assets.stickpng.com/images/587e31f49686194a55adab6e.png")
            val connection = url.openConnection()
            val inputStream: InputStream = connection.getInputStream()
            s3Client.createBucket(CreateBucketRequest {
                bucket = "shotit"
            })
            s3Client.putObject(PutObjectRequest {
                bucket = "shotit"
                key = "principal.png"
                body = inputStream.asByteStream()
                contentType = "image/png"
            })
        }
        mockMvc.perform(get("/api/videos/${video.id}/thumbnails/download"))
            .andExpectAll(
                status().isOk,
                header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"images.zip\""),
                header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM.toString())
            )

    }

    fun authenticated(user: User) {
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)
    }
}
