package br.com.acgj.shotit.core.videos.gateways

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToOutputStream
import aws.smithy.kotlin.runtime.net.url.Url
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.utils.normalizeFilename
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

class S3VideoUploadGateway(
    private val client: S3Client,
    private val logger: Logger = LoggerFactory.getLogger("S3Uploader")
) : VideoUploadGateway {

    private final val BUCKET: String = "shotit"

    override suspend fun upload(video: Video): String {
        val file = video.file!!
        val fileKey = normalizeFilename(file.originalFilename!!)

        val request = PutObjectRequest {
            bucket = BUCKET
            key = fileKey
            body = file.inputStream.asByteStream()
            contentType = "video/mp4"
            contentEncoding = "UTF-8"
        }

        client.putObject(request)

        logger.info("S3: Finished Upload for ${file.originalFilename!!}")

        return fileKey
    }

}
