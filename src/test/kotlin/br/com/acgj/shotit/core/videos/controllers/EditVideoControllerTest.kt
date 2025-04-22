package br.com.acgj.shotit.core.videos.controllers

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.infra.auth.UserDetailsImpl
import br.com.acgj.shotit.core.videos.ports.UpdateThumbnailFavorite
import br.com.acgj.shotit.core.videos.ports.UpdateVideoTags
import br.com.acgj.shotit.core.videos.ports.UpdateVideoTitleRequest
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApplicationUser::class, InfraContainersForTestConfiguration::class)
class EditVideoControllerTest : LocalstackTestContainerConfiguration(){

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tagRepository: VideoCategoryTagRepository

    @Autowired
    private lateinit var thumbnailRepository: ThumbnailRepository

    @Autowired
    private lateinit var entityManager: EntityManager


    private lateinit var user: User
    private lateinit var video: Video
    private lateinit var tag: VideoCategoryTag
    private lateinit var firstThumbnail: Thumbnail
    private lateinit var secondThumbnail: Thumbnail
    private lateinit var newTag: VideoCategoryTag

    @BeforeEach
    fun setup() {
        user = User(
            username = "testuser",
            name = "Test User",
            email = "test@test.com",
            password = "password123"
        )
        userRepository.save(user)

        video = Video(
            user = user,
            name = "Test Video",
            status = VideoStatus.SUCCESS
        )
        videoRepository.save(video)
        tag = tagRepository.save(VideoCategoryTag(name = "test-tag"))
        newTag = tagRepository.save(VideoCategoryTag(name = "new-tag"))
        tag.video = mutableListOf(video)
        video.tags = mutableListOf(tag)
        videoRepository.save(video)
        firstThumbnail = thumbnailRepository.save(Thumbnail(video = video, url = "principal.png", principal = true))
        secondThumbnail = thumbnailRepository.save(Thumbnail(video = video, url = "second.png", principal = false))
        val thumbnails = mutableListOf(firstThumbnail, secondThumbnail)
        video.thumbnails = thumbnails
        videoRepository.save(video)
    }

    private fun autheticate() {
        val userDetails = UserDetailsImpl(user)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        val securityContext = SecurityContextImpl()
        securityContext.authentication = authentication
        SecurityContextHolder.setContext(securityContext)
    }

    @Test
    @Transactional
    fun `should update video title when user is authenticated`() {
        autheticate()
        val request = UpdateVideoTitleRequest("New Video Title")

        mockMvc.perform(
            patch("/api/videos/${video.id}/change-name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
        entityManager.refresh(video)
        assertThat(video.name).isEqualTo(request.title)
    }

    @Test
    @Transactional
    fun `should update video tags`() {
        autheticate()
        val request = UpdateVideoTags(mutableListOf(tag.id!!,  newTag.id!!))

        mockMvc.perform(
            patch("/api/videos/${video.id}/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
        entityManager.flush()
        entityManager.refresh(video)
        assertThat(video.tags?.size).isEqualTo(request.tagIds.size)
    }

    @Test
    @Transactional
    fun `should update favorite thumbnail`() {
        autheticate()
        val request = UpdateThumbnailFavorite(secondThumbnail.id!!)

        mockMvc.perform(
            patch("/api/videos/${video.id}/thumbnails/favorite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
        entityManager.flush()
        entityManager.refresh(secondThumbnail)
        assertThat(secondThumbnail.principal).isTrue()
    }

    @Test
    @Transactional
    fun `should throw bad request when user is not authenticated`() {
        val request = UpdateVideoTitleRequest("New Video Title")

        mockMvc.perform(
            patch("/api/videos/${video.id}/change-name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().`is`(403))
    }

    @Test
    @Transactional
    fun `should throw bad request when video is not found`() {
        autheticate()
        val request = UpdateVideoTitleRequest("New Video Title")
        val nonExistentVideoId = 99999L

        mockMvc.perform(
            patch("/api/videos/$nonExistentVideoId/change-name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
} 