package br.com.acgj.shotit.core.videos.gateways

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.smithy.kotlin.runtime.net.url.Url
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.utils.normalizeFilename
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class S3VideoUploadGatewayTest : LocalstackTestContainerConfiguration() {

    private lateinit var s3Client: S3Client
    private lateinit var gateway: S3VideoUploadGateway

    @BeforeEach
    fun setup() {
        runBlocking {
            s3Client = S3Client {
                region = "us-east-1"
                endpointUrl = Url.parse(localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString())
                credentialsProvider = StaticCredentialsProvider {
                    accessKeyId = "test"
                    secretAccessKey = "test"
                }
            }

            s3Client.createBucket(CreateBucketRequest { bucket = "shotit" })
            gateway = S3VideoUploadGateway(s3Client)

        }

    }

    @Test
    fun `should upload video file to s3 and return file key`() {
        val videoContent = "test video content".toByteArray()
        val videoFile = MockMultipartFile(
            "video.mp4",
            "video.mp4",
            "video/mp4",
            videoContent
        )
        val video = Video(
            name = "Test Video",
            file = videoFile
        )

        val fileKey = runBlocking { gateway.upload(video) }

        assertNotNull(fileKey)
        assertEquals(normalizeFilename("video.mp4"), fileKey)
    }
} 