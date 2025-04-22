package br.com.acgj.shotit.core.auth.gateways

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import br.com.acgj.shotit.core.domain.Thumbnail
import br.com.acgj.shotit.utils.normalizeFilename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class S3AvatarUploadGateway(private val client: S3Client) {

    companion object {
        const val BUCKET_NAME = "shotit"
    }

    suspend fun upload(username: String, image: MultipartFile): String {
        val normalized = normalizeFilename(image.originalFilename!!)
        val fileKey = "${username}_${normalized}"

        val request = PutObjectRequest {
            bucket = BUCKET_NAME
            key = fileKey
            body = image.inputStream.asByteStream()
            contentType = image.contentType
            contentEncoding = "UTF-8"
        }

        client.use { it.putObject(request) }

        val endpoint = client.config.endpointUrl.toString()

        return "${endpoint}/$BUCKET_NAME/${fileKey}"
    }

    suspend fun retrieve(thumbnails: List<Thumbnail>): ByteArray {
        if (thumbnails.isEmpty()) {
            return ByteArray(0)
        }

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            thumbnails.forEach { thumbnail ->
                val fileKey = thumbnail.url.split("/").last()
                val request = GetObjectRequest {
                    bucket = BUCKET_NAME
                    key = fileKey
                }

                client.getObject(request) { response ->
                    val content = response.body!!.toByteArray()
                    val entry = ZipEntry("thumb_${thumbnail.id}.png")
                    zip.putNextEntry(entry)
                    zip.write(content)
                    zip.closeEntry()
                }
            }
        }

        return output.toByteArray()
    }
}
