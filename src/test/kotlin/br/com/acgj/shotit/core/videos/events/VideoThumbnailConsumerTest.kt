package br.com.acgj.shotit.core.videos.events

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.infra.messaging.RabbitMQVideoConfiguration
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ContextConfiguration(classes = [InfraContainersForTestConfiguration::class])
class VideoThumbnailConsumerTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var videoThumbnailConsumer: VideoThumbnailConsumer

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var thumbnailRepository: ThumbnailRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    @Transactional
    fun `should process thumbnail event and update video status to success`() {
        val video = Video(
            name = "test video",
            status = VideoStatus.PENDING
        )
        videoRepository.save(video)

        val event = UploadedThumbnailEvent(
            video = video.id!!,
            urls = listOf("http://example.com/thumb1.jpg", "http://example.com/thumb2.jpg")
        )

        videoThumbnailConsumer.handleAddThumbnails(event)

        val updatedVideo = videoRepository.findById(video.id!!).get()
        assertEquals(VideoStatus.SUCCESS, updatedVideo.status)
        entityManager.refresh(updatedVideo)
        val thumbnails = updatedVideo.thumbnails!!
        assertEquals(2, thumbnails.size)
        assertTrue(thumbnails.any { it.principal })
        assertEquals("http://example.com/thumb1.jpg", thumbnails.first { it.principal }.url)
    }

    @Test
    @Transactional
    fun `should handle single thumbnail and mark it as principal`() {
        val video = Video(
            name = "test video",
            status = VideoStatus.PENDING
        )
        videoRepository.save(video)

        val event = UploadedThumbnailEvent(
            video = video.id!!,
            urls = listOf("http://example.com/thumb1.jpg")
        )

        videoThumbnailConsumer.handleAddThumbnails(event)

        val updatedVideo = videoRepository.findById(video.id!!).get()
        assertEquals(VideoStatus.SUCCESS, updatedVideo.status)

        val thumbnails = thumbnailRepository.findCurrentFavorite(video)
        assertEquals("http://example.com/thumb1.jpg", thumbnails.get().url)
    }
} 