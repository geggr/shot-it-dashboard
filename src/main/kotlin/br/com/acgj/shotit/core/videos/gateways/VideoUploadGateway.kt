package br.com.acgj.shotit.core.videos.gateways

import br.com.acgj.shotit.core.domain.Video
import java.io.ByteArrayOutputStream

interface VideoUploadGateway {
    suspend fun upload(video: Video) : String
}