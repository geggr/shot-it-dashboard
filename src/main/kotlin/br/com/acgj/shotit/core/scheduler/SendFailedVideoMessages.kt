package br.com.acgj.shotit.core.scheduler

import br.com.acgj.shotit.core.domain.VideoRepository
import br.com.acgj.shotit.core.domain.VideoStatus
import br.com.acgj.shotit.core.infra.mailer.AWSMailer
import br.com.acgj.shotit.core.videos.mails.FailedToUploadEmail
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SendFailedVideoMessages(
    val mailer: AWSMailer,
    val repository: VideoRepository,
    val logger: Logger = LoggerFactory.getLogger(SendPendingVideosToQueue::class.java)
) {

    @Scheduled(cron = "0 */3 * * * *")
    fun handleSendFailedVideoEmail(){
        logger.info("Sending Failed Video")
        repository
            .findAllByStatus(VideoStatus.FAILED)
            .map {
                runBlocking {
                    logger.info("Sending Failed Video ${it.id}")
                    mailer.send(
                        user = it.user!!,
                        email = FailedToUploadEmail(it)
                    )
                }
            }
    }
}
