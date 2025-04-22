package br.com.acgj.shotit.core.infra.mailer

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.VerifyEmailAddressRequest
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.VideoRepository
import br.com.acgj.shotit.core.infra.cloud.AwsConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Import(AwsConfiguration::class, InfraContainersForTestConfiguration::class)
@ActiveProfiles("test")
class AWSMailerTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var awsMailer: AWSMailer

    @Autowired
    private lateinit var sesClient: SesClient

    @Test
    fun `should send email successfully`() {
        runBlocking {
            sesClient.verifyEmailAddress(
                VerifyEmailAddressRequest {
                    emailAddress = "no-reply@shotit.com"
                }
            )
        }
        runBlocking {
            val user = User(
                username = "testuser",
                name = "Test User",
                email = "test@example.com",
                password = "password123"
            )

            val emailTemplate = object : EmailTemplate {
                override val subject: String = "Test Subject"
                override fun body(): String = "<h1>Test Body</h1>"
            }

            awsMailer.send(user, emailTemplate)
        }
    }
} 