package br.com.acgj.shotit.core.videos.events

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.core.domain.VideoStatus
import br.com.acgj.shotit.core.infra.messaging.RabbitMQVideoConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import(InfraContainersForTestConfiguration::class)
class UploadVideoProducerTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var uploadVideoProducer: UploadVideoProducer

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Test
    fun `should send video upload event to rabbitmq when video is created`() {
        val video = Video(
            id = 1L,
            name = "test-video.mp4",
            status = VideoStatus.PENDING
        )

        uploadVideoProducer.createdVideo(video)

        val message = rabbitTemplate.receive(RabbitMQVideoConfiguration.UPLOAD_QUEUE, 5000)
        assertThat(message).isNotNull
        assertThat(message?.body).isNotNull

        val event = String(message?.body ?: ByteArray(0))
        assertThat(event).contains("\"id\":1")
        assertThat(event).contains("\"url\":\"test-video.mp4\"")
        assertThat(event).contains("\"timestamp\"")
    }
} 