package br.com.acgj.shotit.core.videos.services

import br.com.acgj.shotit.core.domain.Video
import java.io.ByteArrayOutputStream

interface UploadVideoService {
    suspend fun upload(videos: List<Video>)
}
