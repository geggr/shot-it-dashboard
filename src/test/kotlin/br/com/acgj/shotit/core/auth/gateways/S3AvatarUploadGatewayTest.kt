package br.com.acgj.shotit.core.auth.gateways

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import br.com.acgj.shotit.core.domain.Thumbnail
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.InputStream
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@Testcontainers
class S3AvatarUploadGatewayTest {
    companion object {
        @Container
        val localStack: LocalStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0.0"))
            .withServices(LocalStackContainer.Service.S3)
    }

    private lateinit var s3Client: S3Client
    private lateinit var gateway: S3AvatarUploadGateway

    @BeforeEach
    fun setup() {
        s3Client = S3Client {
            endpointUrl = Url.parse(localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString())
            region = localStack.region
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = localStack.accessKey,
                    secretAccessKey = localStack.secretKey
                )
            )
        }
        gateway = S3AvatarUploadGateway(s3Client)

        // Create test bucket
        runBlocking {
            s3Client.createBucket(CreateBucketRequest {
                bucket = "shotit"
            })
        }
    }

    @Test
    fun `upload should successfully upload file to S3`() {
        // Arrange
        val username = "testuser"
        val fileContent = "test image content".toByteArray()
        val mockFile = MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            fileContent
        )
        val expectedUrl = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/testuser_test.jpg"

        // Act
        val result = runBlocking { gateway.upload(username, mockFile) }

        // Assert
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `upload should handle null content type`() {
        // Arrange
        val username = "testuser"
        val fileContent = "test image content".toByteArray()
        val mockFile = MockMultipartFile(
            "test.jpg",
            "test.jpg",
            null,
            fileContent
        )
        val expectedUrl = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/testuser_test.jpg"

        // Act
        val result = runBlocking { gateway.upload(username, mockFile) }

        // Assert
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `upload should handle special characters in filename`() {
        // Arrange
        val username = "testuser"
        val fileContent = "test image content".toByteArray()
        val mockFile = MockMultipartFile(
            "test image with spaces.jpg",
            "test image with spaces.jpg",
            "image/jpeg",
            fileContent
        )
        val expectedUrl = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/testuser_test_image_with_spaces.jpg"

        // Act
        val result = runBlocking { gateway.upload(username, mockFile) }

        // Assert
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `retrieve should successfully download and zip thumbnails`() {
        upload("thumb1.png")
        upload("thumb2.png")

        val thumbnails = listOf(
            Thumbnail(id = 1, url = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/thumb1.png"),
            Thumbnail(id = 2, url = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/thumb2.png")
        )

        // Act
        val result = runBlocking { gateway.retrieve(thumbnails) }

        // Assert
        assertNotNull(result)
        assert(result.isNotEmpty()) { "ZIP file should not be empty" }
    }

    @Test
    fun `retrieve should handle empty thumbnail list`() {
        // Arrange
        val emptyThumbnails = emptyList<Thumbnail>()

        // Act
        val result = runBlocking { gateway.retrieve(emptyThumbnails) }

        // Assert
        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `retrieve should throw NoSuchKeyException when thumbnail does not exist`() {
        // Arrange
        val thumbnails = listOf(
            Thumbnail(id = 1, url = "${localStack.getEndpointOverride(LocalStackContainer.Service.S3)}/shotit/non_existent.png")
        )

        // Act & Assert
        assertFailsWith<NoSuchKey> {
            runBlocking { gateway.retrieve(thumbnails) }
        }
    }

    fun upload(path: String){
        runBlocking {
            val url = URL("https://assets.stickpng.com/images/587e31f49686194a55adab6e.png")
            val connection = url.openConnection()
            val inputStream: InputStream = connection.getInputStream()
            s3Client.createBucket(CreateBucketRequest {
                bucket = "shotit"
            })
            s3Client.putObject(PutObjectRequest {
                bucket = "shotit"
                key =  path
                body = inputStream.asByteStream()
                contentType = "image/png"
            })
        }
    }
} 