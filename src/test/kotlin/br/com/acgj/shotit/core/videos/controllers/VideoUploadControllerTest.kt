package br.com.acgj.shotit.core.videos.controllers

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.core.domain.VideoRepository
import br.com.acgj.shotit.core.domain.VideoStatus
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import br.com.acgj.shotit.core.videos.ports.SignVideoUploadRequest
import br.com.acgj.shotit.core.videos.ports.UploadVideoRequest
import br.com.acgj.shotit.core.videos.services.SignVideoService
import br.com.acgj.shotit.core.videos.services.UploadVideoService
import br.com.acgj.shotit.core.videos.services.VideoService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(ApplicationUser::class, InfraContainersForTestConfiguration::class )
class VideoUploadControllerTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var applicationUser: ApplicationUser

    @Autowired
    private lateinit var videoService: VideoService

    @Autowired
    private lateinit var uploadService: UploadVideoService

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var signService: SignVideoService

    @Test
    fun `should upload video successfully`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password"
        )
        authenticated(user)
        
        val file1 = MockMultipartFile(
            "files",
            "test-video1.mp4",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            "test content 1".toByteArray()
        )
        val file2 = MockMultipartFile(
            "files",
            "test-video2.mp4",
            MediaType.MULTIPART_FORM_DATA_VALUE,
            "test content 2".toByteArray()
        )

        mockMvc.multipart("/api/videos") {
            file(file1)
            file(file2)
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `should sign video successfully`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password"
        )
        authenticated(user)
        val video = Video(
            name = "test-video.mp4",
            user = user,
            status = VideoStatus.PENDING
        )
        videoRepository.save(video)

        val request = SignVideoUploadRequest(
            names = listOf("test-video.mp4")
        )

        mockMvc.post("/api/videos/sign") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpectAll {
            status { isOk() }
        }
    }

    @Test
    fun `should complete video upload successfully`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password"
        )
        authenticated(user)
        val video = Video(
            name = "test-video.mp4",
            user = user,
            status = VideoStatus.UPLOADED
        )
        videoRepository.save(video)

        mockMvc.post("/api/videos/${video.id}/complete") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
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