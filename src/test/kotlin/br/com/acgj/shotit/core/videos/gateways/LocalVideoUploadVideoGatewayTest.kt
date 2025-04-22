package br.com.acgj.shotit.core.videos.gateways

import br.com.acgj.shotit.core.domain.Video
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class LocalVideoUploadVideoGatewayTest {
    private lateinit var gateway: LocalVideoUploadVideoGateway
    private lateinit var testFolder: Path
    private lateinit var resourceLoader: ResourceLoader

    @BeforeEach
    fun setup() {
        testFolder = Files.createTempDirectory("test-videos")
        resourceLoader = DefaultResourceLoader()
        gateway = LocalVideoUploadVideoGateway(
            loader = resourceLoader,
            folderName = testFolder.toString()
        )
    }

    @AfterEach
    fun cleanup() {
        Files.walk(testFolder)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.delete(it) }
    }

    @Test
    fun `should upload video file successfully`() {
        val videoFile = MockMultipartFile(
            "video.mp4",
            "video.mp4",
            "video/mp4",
            "test video content".toByteArray()
        )
        val video = Video(
            name = "Test Video",
            file = videoFile
        )

        val result = runBlocking { gateway.upload(video) }

        assertTrue(Files.exists(Path.of(result)))
        assertEquals(testFolder.resolve("video.mp4").toString(), result)
    }

    @Test
    fun `should generate random filename when original filename is null`() {
        val videoFile = MockMultipartFile(
            "video",
            null,
            "video/mp4",
            "test video content".toByteArray()
        )
        val video = Video(
            name = "Test Video",
            file = videoFile
        )

        val result = runBlocking { gateway.upload(video) }

        assertTrue(Files.exists(Path.of(result)))
        assertTrue(result.contains(testFolder.toString()))
    }

    @Test
    fun `should create directory if it does not exist`() {
        val nonExistentFolder = testFolder.resolve("new-folder")
        gateway = LocalVideoUploadVideoGateway(
            loader = resourceLoader,
            folderName = nonExistentFolder.toString()
        )

        val videoFile = MockMultipartFile(
            "video.mp4",
            "video.mp4",
            "video/mp4",
            "test video content".toByteArray()
        )
        val video = Video(
            name = "Test Video",
            file = videoFile
        )

        val result = runBlocking { gateway.upload(video) }

        assertTrue(Files.exists(nonExistentFolder))
        assertTrue(Files.exists(Path.of(result)))
    }

    @Test
    fun `should throw exception when video file is null`() {
        val video = Video(
            name = "Test Video",
            file = null
        )

        assertFailsWith<NullPointerException> {
            runBlocking { gateway.upload(video) }
        }
    }

    @Test
    fun `should handle file upload failure gracefully`() {
        val videoFile = MockMultipartFile(
            "video.mp4",
            "video.mp4",
            "video/mp4",
            "test video content".toByteArray()
        )
        val video = Video(
            name = "Test Video",
            file = videoFile
        )

        // Make the directory read-only to simulate a failure
        testFolder.toFile().setReadOnly()

        assertFailsWith<Exception> {
            runBlocking { gateway.upload(video) }
        }

        // Restore permissions for cleanup
        testFolder.toFile().setWritable(true)
    }
} 