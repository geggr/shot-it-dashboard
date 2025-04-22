package br.com.acgj.shotit.core.videos.gateways

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import br.com.acgj.shotit.core.domain.Thumbnail
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class S3ThumbnailGateway(private val client: S3Client) : ThumbnailDownloadGateway {

    companion object {
        const val BUCKET_NAME = "shotit"
    }

    override suspend fun retrieve(thumbnails: List<Thumbnail>): ByteArray {
        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { zip ->
            thumbnails.map { thumbnail ->

                val fileKey = thumbnail.url.split("/").last()

                val request = GetObjectRequest {
                    bucket = BUCKET_NAME
                    key = fileKey
                }
                client.getObject(request) { response ->
                    val entry = ZipEntry("thumb_${thumbnail.id}.png")
                    zip.putNextEntry(entry)
                    zip.write(response.body?.toByteArray()!!)
                    zip.closeEntry()
                }

            }
        }
        return output.toByteArray();
    }
}