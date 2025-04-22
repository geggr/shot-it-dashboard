package br.com.acgj.shotit.core.videos.services

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.*
import br.com.acgj.shotit.core.videos.gateways.S3ThumbnailGateway
import br.com.acgj.shotit.core.videos.ports.VideoDTO
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.testcontainers.containers.localstack.LocalStackContainer
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.util.*

@SpringBootTest
@Import(InfraContainersForTestConfiguration::class)
class RetrieveVideoServiceTest : LocalstackTestContainerConfiguration() {

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var videoCategoryTagRepository: VideoCategoryTagRepository

    private lateinit var s3Client: S3Client
    private lateinit var thumbnailGateway: S3ThumbnailGateway
    private lateinit var service: RetrieveVideoService
    private lateinit var user: User
    private lateinit var video: Video
    private lateinit var tag: VideoCategoryTag

    @BeforeEach
    fun setup() {
        s3Client = S3Client {
            endpointUrl = Url.parse(localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString())
            region = localstack.region
            credentialsProvider = StaticCredentialsProvider(
                Credentials(
                    accessKeyId = localstack.accessKey,
                    secretAccessKey = localstack.secretKey
                )
            )
        }

        thumbnailGateway = S3ThumbnailGateway(s3Client)
        service = RetrieveVideoService(videoRepository, thumbnailGateway)

        // Create test bucket
        runBlocking {
            s3Client.createBucket(CreateBucketRequest {
                bucket = "shotit"
            })
        }

        user = User(
            username = "testuser", name = "Test User", email = "test@test.com", password = "password123"
        )
        user = userRepository.save(user)

        video = Video(
            user = user, name = "Test Video", status = VideoStatus.SUCCESS
        )
        video = videoRepository.save(video)

        tag = VideoCategoryTag(name = "test tag")
        tag = videoCategoryTagRepository.save(tag)

        video.tags = mutableListOf(tag)
        video = videoRepository.save(video)
    }

    @Test
    fun `should retrieve videos for user`() {
        val result = service.retrieve(user)

        assertEquals(1, result?.size)
        assertEquals(video.id, result?.first()?.id)
        assertEquals(video.name, result?.first()?.name)
        assertEquals(video.status.name, result?.first()?.status)
        assertEquals(1, result?.first()?.tags?.size)
        assertEquals(tag.name, result?.first()?.tags?.first()?.name)
    }

    @Test
    fun `should find video by id`() {
        val result = service.findById(video.id!!)

        assert(result.isPresent)
        assertEquals(video.id, result.get().id)
        assertEquals(video.name, result.get().name)
        assertEquals(video.status.name, result.get().status)
        assertEquals(1, result.get().tags.size)
        assertEquals(tag.name, result.get().tags.first().name)
    }

    @Test
    fun `should return empty optional when video not found by id`() {
        val result = service.findById(999L)

        assertEquals(Optional.empty<VideoDTO>(), result)
    }

    @Test
    fun `should download thumbnails successfully`() {
        val thumbnail = Thumbnail(video = video, url = "http://example.com/principal.png")
        video.thumbnails = mutableListOf(thumbnail)
        video = videoRepository.save(video)

        // Upload test thumbnail to S3
        runBlocking {
            runBlocking {
                val url = URL("https://assets.stickpng.com/images/587e31f49686194a55adab6e.png")
                val connection = url.openConnection()
                val inputStream: InputStream = connection.getInputStream()
                s3Client.putObject(PutObjectRequest {
                    bucket = "shotit"
                    key = "principal.png"
                    body = inputStream.asByteStream()
                    contentType = "image/png"
                })
            }
        }

        val result = runBlocking { service.downloadThumbnails(video.id!!) }

        assert(result.isNotEmpty())
    }

    @Test
    fun `should throw not found error when video does not exist`() {
        assertThrows(NotFoundError::class.java) {
            runBlocking {
                service.downloadThumbnails(999L)
            }
        }
    }

    @Test
    fun `should throw not found error when video has no thumbnails`() {
        video.thumbnails = mutableListOf()
        video = videoRepository.save(video)

        assertThrows(NotFoundError::class.java) {
            runBlocking {
                service.downloadThumbnails(video.id!!)
            }
        }
    }

    @Test
    fun `should throw not found error when video has null thumbnails`() {
        video.thumbnails = null
        video = videoRepository.save(video)

        assertThrows(NotFoundError::class.java) {
            runBlocking {
                service.downloadThumbnails(video.id!!)
            }
        }
    }
} 