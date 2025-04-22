package br.com.acgj.shotit.core.scheduler

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.UserRepository
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.core.domain.VideoStatus
import br.com.acgj.shotit.core.videos.events.UploadedVideoEvent
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Import(InfraContainersForTestConfiguration::class)
@ActiveProfiles("test")
class SendPendingVideosToQueueTest  : LocalstackTestContainerConfiguration(){

    @Autowired
    private lateinit var sendPendingVideosToQueue: SendPendingVideosToQueue

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Test
    @Transactional
    fun `should send pending videos to queue`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val video = Video(
            name = "test-video.mp4",
            status = VideoStatus.PENDING,
            user = user
        )

        userRepository.save(user)
        sendPendingVideosToQueue.repository.save(video)

        sendPendingVideosToQueue.handleSendToQueue()

        val uploadedVideoEvent = rabbitTemplate.receiveAndConvert("video.processed", 1000) as UploadedVideoEvent
        assertNotNull(uploadedVideoEvent)
        assertEquals(video.id, uploadedVideoEvent.id)
    }

    @Test
    @Transactional
    fun `should not send non pending videos to queue`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val video = Video(
            name = "test-video.mp4",
            status = VideoStatus.SUCCESS,
            user = user
        )

        userRepository.save(user)
        sendPendingVideosToQueue.repository.save(video)

        sendPendingVideosToQueue.handleSendToQueue()

        val uploadedVideoEvent = rabbitTemplate.receiveAndConvert("video.processed", 1000)
        assertNull(uploadedVideoEvent)
    }

    @Test
    @Transactional
    fun `should handle multiple pending videos`() {
        val user1 = User(
            username = "testuser1",
            name = "Test User 1",
            email = "test1@example.com",
            password = "password123"
        )
        userRepository.save(user1)

        val video1 = Video(
            name = "test-video-1.mp4",
            status = VideoStatus.PENDING,
            user = user1
        )

        sendPendingVideosToQueue.repository.save(video1)

        val user2 = User(
            username = "testuser2",
            name = "Test User 2",
            email = "test2@example.com",
            password = "password123"
        )

        val video2 = Video(
            name = "test-video-2.mp4",
            status = VideoStatus.PENDING,
            user = user2
        )

        userRepository.save(user2)
        sendPendingVideosToQueue.repository.save(video2)

        sendPendingVideosToQueue.handleSendToQueue()

        var uploadedVideoEvent = rabbitTemplate.receiveAndConvert("video.processed", 1000) as UploadedVideoEvent
        assertNotNull(uploadedVideoEvent)
        assertEquals(video1.id, uploadedVideoEvent.id)

        uploadedVideoEvent = rabbitTemplate.receiveAndConvert("video.processed", 1000) as UploadedVideoEvent
        assertNotNull(uploadedVideoEvent)
        assertEquals(video2.id, uploadedVideoEvent.id)
    }
}