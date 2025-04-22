package br.com.acgj.shotit.core.videos.ports

import br.com.acgj.shotit.core.domain.Thumbnail
import br.com.acgj.shotit.core.domain.Video
import br.com.acgj.shotit.core.domain.VideoCategoryTag
import br.com.acgj.shotit.core.domain.VideoStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime

class VideoOutputTest {

    @Test
    fun `should create SignedVideoDTO with correct values`() {
        val signedVideo = SignedVideoDTO(1L, "https://example.com/video.mp4", "video.mp4")
        
        assertEquals(1L, signedVideo.id)
        assertEquals("https://example.com/video.mp4", signedVideo.url)
        assertEquals("video.mp4", signedVideo.filename)
    }

    @Test
    fun `should create VideoThumbnail from Thumbnail entity`() {
        val thumbnail = Thumbnail(
            id = 1L,
            video = null,
            url = "https://example.com/thumb.jpg",
            principal = false
        )
        
        val videoThumbnail = VideoThumbnail(thumbnail)
        
        assertEquals(1L, videoThumbnail.id)
        assertEquals("https://example.com/thumb.jpg", videoThumbnail.url)
    }

    @Test
    fun `should create VideoTags from VideoCategoryTag entity`() {
        val tag = VideoCategoryTag(
            id = 1L,
            video = null,
            name = "Action"
        )
        
        val videoTags = VideoTags(tag)
        
        assertEquals(1L, videoTags.id)
        assertEquals("Action", videoTags.name)
    }

    @Test
    fun `should create VideoDTO from Video entity with empty lists`() {
        val video = Video(
            id = 1L,
            createdAt = LocalDateTime.now(),
            user = null,
            thumbnails = null,
            tags = null,
            status = VideoStatus.PENDING,
            name = "Test Video",
            url = "https://example.com/video.mp4",
            file = null,
            originalName = null
        )
        
        val videoDTO = VideoDTO(video, emptyList())
        
        assertEquals(1L, videoDTO.id)
        assertEquals("Test Video", videoDTO.name)
        assertEquals("PENDING", videoDTO.status)
        assertEquals("https://example.com/video.mp4", videoDTO.url)
        assertEquals(emptyList<VideoThumbnail>(), videoDTO.thumbnails)
        assertEquals(emptyList<VideoTags>(), videoDTO.tags)
    }

    @Test
    fun `should create VideoDTO from Video entity with thumbnails and tags`() {
        val video = Video(
            id = 1L,
            createdAt = LocalDateTime.now(),
            user = null,
            thumbnails = mutableListOf(
                Thumbnail(
                    id = 1L,
                    video = null,
                    url = "https://example.com/thumb1.jpg",
                    principal = false
                ),
                Thumbnail(
                    id = 2L,
                    video = null,
                    url = "https://example.com/thumb2.jpg",
                    principal = false
                )
            ),
            tags = null,
            status = VideoStatus.PENDING,
            name = "Test Video",
            url = "https://example.com/video.mp4",
            file = null,
            originalName = null
        )
        
        val tags = listOf(
            VideoCategoryTag(
                id = 1L,
                video = null,
                name = "Action"
            ),
            VideoCategoryTag(
                id = 2L,
                video = null,
                name = "Adventure"
            )
        )
        
        val videoDTO = VideoDTO(video, tags)
        
        assertEquals(1L, videoDTO.id)
        assertEquals("Test Video", videoDTO.name)
        assertEquals("PENDING", videoDTO.status)
        assertEquals("https://example.com/video.mp4", videoDTO.url)
        assertEquals(2, videoDTO.thumbnails.size)
        assertEquals(2, videoDTO.tags.size)
        
        assertEquals(1L, videoDTO.thumbnails[0].id)
        assertEquals("https://example.com/thumb1.jpg", videoDTO.thumbnails[0].url)
        assertEquals(2L, videoDTO.thumbnails[1].id)
        assertEquals("https://example.com/thumb2.jpg", videoDTO.thumbnails[1].url)
        
        assertEquals(1L, videoDTO.tags[0].id)
        assertEquals("Action", videoDTO.tags[0].name)
        assertEquals(2L, videoDTO.tags[1].id)
        assertEquals("Adventure", videoDTO.tags[1].name)
    }
} 