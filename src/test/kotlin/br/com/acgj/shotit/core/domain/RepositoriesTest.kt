package br.com.acgj.shotit.core.domain

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(InfraContainersForTestConfiguration::class)
class RepositoriesTest{

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var user: User
    private lateinit var video: Video

    @BeforeEach
    fun setup() {
        user = User(
            username = "testuser",
            name = "Test User",
            email = "test@test.com",
            password = "password123"
        )
        userRepository.save(user)

        video = Video(
            user = user,
            name = "Test Video",
            status = VideoStatus.PENDING
        )
        videoRepository.save(video)
    }

    @Test
    fun `should fetch video with thumbnails when using eager loading by id`() {
        val thumbnail = Thumbnail(video = video, url = "http://example.com/thumb.jpg")
        video.thumbnails = mutableListOf(thumbnail)
        videoRepository.save(video)

        val found = videoRepository.eagerFindById(video.id!!)
        
        assertThat(found).isPresent
        assertThat(found.get().thumbnails).hasSize(1)
        assertThat(found.get().thumbnails?.first()?.url).isEqualTo("http://example.com/thumb.jpg")
    }

    @Test
    fun `should return all videos for a specific user`() {
        val secondVideo = Video(
            user = user,
            name = "Second Test Video",
            status = VideoStatus.PENDING
        )
        videoRepository.save(secondVideo)

        val videos = videoRepository.findByUser(user)
        
        assertThat(videos).hasSize(2)
        assertThat(videos.map { it.name }).containsExactlyInAnyOrder("Test Video", "Second Test Video")
    }

    @Test
    fun `should update video title when user owns the video`() {
        val newName = "Updated Video Title"
        
        val updated = videoRepository.updateVideoTitle(newName, video.id!!, user)
        entityManager.flush()
        entityManager.refresh(video)
        
        assertThat(updated).isEqualTo(1)
        assertThat(video.name).isEqualTo(newName)
    }

    @Test
    fun `should find all videos by status and include user information`() {
        val secondUser = User(
            username = "testuser2",
            name = "Test User 2",
            email = "test2@test.com",
            password = "password123"
        )
        userRepository.save(secondUser)

        val successVideo = Video(
            user = secondUser,
            name = "Success Video",
            status = VideoStatus.SUCCESS
        )
        videoRepository.save(successVideo)

        val pendingVideos = videoRepository.findAllByStatus(VideoStatus.PENDING)
        val successVideos = videoRepository.findAllByStatus(VideoStatus.SUCCESS)
        
        assertThat(pendingVideos).hasSize(1)
        assertThat(pendingVideos[0].status).isEqualTo(VideoStatus.PENDING)
        assertThat(pendingVideos[0].user?.username).isEqualTo("testuser")

        assertThat(successVideos).hasSize(1)
        assertThat(successVideos[0].status).isEqualTo(VideoStatus.SUCCESS)
        assertThat(successVideos[0].user?.username).isEqualTo("testuser2")
    }

    @Test
    fun `should return empty list when no videos exist with given status`() {
        val failedVideos = videoRepository.findAllByStatus(VideoStatus.FAILED)
        
        assertThat(failedVideos).isEmpty()
    }
} 