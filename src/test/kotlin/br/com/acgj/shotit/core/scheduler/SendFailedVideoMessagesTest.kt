package br.com.acgj.shotit.core.scheduler

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.VerifyEmailAddressRequest
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.infra.cloud.AwsConfiguration
import br.com.acgj.shotit.core.infra.mailer.AWSMailer
import br.com.acgj.shotit.core.videos.mails.FailedToUploadEmail
import jakarta.persistence.EntityManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@Import(AWSMailer::class, SendFailedVideoMessages::class, AwsConfiguration::class, InfraContainersForTestConfiguration::class)
class SendFailedVideoMessagesTest: LocalstackTestContainerConfiguration() {


    private lateinit var sendFailedVideoMessages: SendFailedVideoMessages

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var sesClient: SesClient

    @Autowired
    private lateinit var mailer: AWSMailer

    @BeforeEach
    fun setup() {
        // verifing email no-reply@shotit.com
        runBlocking {
            sesClient.verifyEmailAddress(
                VerifyEmailAddressRequest {
                    emailAddress = "no-reply@shotit.com"
                }
            )
        }
        sendFailedVideoMessages = SendFailedVideoMessages(spy(mailer), videoRepository)
    }

    @Test
    @Transactional
    fun `should send email for failed videos`() {
        val user = User(
            username = "testuser",
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        val user2 = User(
            username = "testuser2",
            name = "Test User 2",
            email = "test-2@example.com",
            password = "password123"
        )

        val video = Video(
            name = "test-video.mp4",
            status = VideoStatus.FAILED,
            user = user
        )

        val success = Video(
            name = "test-video-success.mp4",
            status = VideoStatus.SUCCESS,
            user = user
        )

        val success2 = Video(
            name = "test-video-success-2.mp4",
            status = VideoStatus.SUCCESS,
            user = user2
        )

        userRepository.save(user)
        videoRepository.save(video)
        videoRepository.save(success)
        entityManager.flush()
        sendFailedVideoMessages.handleSendFailedVideoEmail()

        runBlocking {
            verify(sendFailedVideoMessages.mailer).send(
                user = user,
                email = FailedToUploadEmail(video)
            )
        }
    }

    @Test
    @Transactional
    fun `should not send email for non failed videos`() {
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
        videoRepository.save(video)

        sendFailedVideoMessages.handleSendFailedVideoEmail()

        runBlocking {
            verify(sendFailedVideoMessages.mailer, never()).send(
              user = user,
              email = FailedToUploadEmail(video)
            )
        }
    }

    @Test
    @Transactional
    fun `should handle multiple failed videos`() {
        val user1 = User(
            username = "testuser1",
            name = "Test User 1",
            email = "test1@example.com",
            password = "password123"
        )

        val user2 = User(
            username = "testuser2",
            name = "Test User 2",
            email = "test2@example.com",
            password = "password123"
        )

        val video1 = Video(
            name = "test-video-1.mp4",
            status = VideoStatus.FAILED,
            user = user1
        )

        val video2 = Video(
            name = "test-video-2.mp4",
            status = VideoStatus.FAILED,
            user = user2
        )

        userRepository.save(user1)
        userRepository.save(user2)
        videoRepository.save(video1)
        videoRepository.save(video2)
        entityManager.flush()
        sendFailedVideoMessages.handleSendFailedVideoEmail()

        runBlocking {
            verify(sendFailedVideoMessages.mailer, times(1)).send(
                user = user1,
                email = FailedToUploadEmail(video1)
            )
            verify(sendFailedVideoMessages.mailer, times(1)).send(
                user = user2,
                email = FailedToUploadEmail(video2)
            )
        }
    }
} 