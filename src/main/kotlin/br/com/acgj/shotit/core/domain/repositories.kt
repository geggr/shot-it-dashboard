package br.com.acgj.shotit.core.domain

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
}

@Repository
interface VideoRepository : JpaRepository<Video, Long> {

    @Query("FROM Video v LEFT JOIN FETCH v.thumbnails WHERE v.id = :id")
    fun eagerFindById(@Param("id") id: Long): Optional<Video>

    @EntityGraph(attributePaths = ["thumbnails"])
    @Query("FROM Video v WHERE v.user = :user")
    fun findByUser(user: User): MutableList<Video>

    @Query("SELECT tag FROM VideoCategoryTag tag JOIN tag.video v WHERE v = :video")
    fun retrieveForVideo(video: Video): MutableList<VideoCategoryTag>

    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.name = :name WHERE v.id = :id AND v.user = :user")
    fun updateVideoTitle(name: String, id: Long, user: User): Int

    @Query("FROM Video v JOIN FETCH v.user WHERE v.status = :status")
    fun findAllByStatus(status: VideoStatus): MutableList<Video>
}

@Repository
interface ThumbnailRepository : JpaRepository<Thumbnail, Long> {
    @Query("FROM Thumbnail t WHERE t.video = :video AND t.principal = true")
    fun findCurrentFavorite(video: Video): Optional<Thumbnail>
}

@Repository
interface VideoCategoryTagRepository: JpaRepository<VideoCategoryTag, Long> {

}