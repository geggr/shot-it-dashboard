package br.com.acgj.shotit.core.videos.services

import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.core.domain.VideoRepository
import br.com.acgj.shotit.core.videos.gateways.VideoUploadGateway
import br.com.acgj.shotit.core.videos.events.UploadVideoProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class UploadVideoServiceImpl(
    private val repository: VideoRepository,
    private val gateway: VideoUploadGateway,
    private val producer: UploadVideoProducer,
    private val logger: Logger = LoggerFactory.getLogger("VideoUploader")
) : UploadVideoService {

    override suspend fun upload(videos: List<Video>) {
        videos.forEach { upload(it) }
    }

    suspend fun upload(video: Video) {
        try {
            val url = gateway.upload(video)
            video.url = url
            repository.save(video)
            producer.createdVideo(video)
        } catch (exception: Exception) {
            logger.info("Failed to upload, reason was: ${exception.message}")
        }
    }

}