package br.com.acgj.shotit.core.videos.services

import br.com.acgj.shotit.core.domain.NotFoundError
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.VideoRepository
import br.com.acgj.shotit.core.videos.gateways.ThumbnailDownloadGateway
import br.com.acgj.shotit.core.videos.ports.VideoDTO
import org.springframework.stereotype.Service
import java.util.*

@Service
class RetrieveVideoService(
    private val videoRepository: VideoRepository,
    private val thumbnailDownloadGateway: ThumbnailDownloadGateway
){

    fun retrieve(user: User): List<VideoDTO>? {
        return videoRepository.findByUser(user).map {
            VideoDTO(
                video = it,
                tags = videoRepository.retrieveForVideo(it)
            )
        }
    }

    fun findById(id: Long): Optional<VideoDTO> {
        return videoRepository.eagerFindById(id).map {
            VideoDTO(
                video = it,
                tags = videoRepository.retrieveForVideo(it)
            )
        }
    }

    suspend fun downloadThumbnails(id: Long): ByteArray {
        val video = videoRepository.eagerFindById(id).orElseThrow {  NotFoundError("Video not found")}
        if(video.thumbnails.isNullOrEmpty()) throw NotFoundError("Video without thumbnails");
        return thumbnailDownloadGateway.retrieve(video.thumbnails!!)
    }

}
