package br.com.acgj.shotit.core.infra.mailer

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.*
import br.com.acgj.shotit.core.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AWSMailer(val client: SesClient, @Value("\${aws.ses.sender}") val sourceEmail: String) {

    suspend fun send(user: User, email: EmailTemplate) : String {
        val message = Message {
            subject = Content { data = email.subject }
            body = Body {
                html = Content {
                    data = email.body()
                }
            }
        }

        val request = SendEmailRequest {
            this.destination = Destination {
                toAddresses = listOf(user.email)
            }
            this.message = message
            this.source = sourceEmail
        }

        val response = client.sendEmail(request)

        return response.messageId
    }
}
