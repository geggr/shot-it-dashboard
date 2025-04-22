package br.com.acgj.shotit.core.videos.controllers

import br.com.acgj.shotit.core.domain.NotFoundError
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.videos.ports.VideoDTO
import br.com.acgj.shotit.core.videos.services.EditVideoService
import br.com.acgj.shotit.core.videos.services.RetrieveVideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Vídeos", description = "APIs de gerenciamento e recuperação de vídeos")
@RestController
@RequestMapping("/api/videos")
class VideoController(
    private val applicationUser: ApplicationUser,
    private val retrieveVideoService: RetrieveVideoService,
    private val editVideoService: EditVideoService
){

    @Operation(summary = "Listar vídeos do usuário", description = "Recupera todos os vídeos do usuário autenticado")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Vídeos recuperados com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @GetMapping
    fun handleRetrieveUserVideos(): ResponseEntity<Any> {
        val user = applicationUser.fromAuthorizedContext()
        val videos = retrieveVideoService.retrieve(user)
        return ResponseEntity.ok().body(videos)
    }

    @Operation(summary = "Obter vídeo por ID", description = "Recupera um vídeo específico pelo seu ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Vídeo recuperado com sucesso"),
        ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    ])
    @GetMapping("/{id}")
    fun handleRetrieveVideo(@PathVariable("id") id: Long): ResponseEntity<VideoDTO>? {
        val video = retrieveVideoService.findById(id);

        return video
            .map { ResponseEntity.ok(it) }
            .orElseThrow{ NotFoundError("Video not found") }
    }

    @Operation(summary = "Download de thumbnails", description = "Faz download de um arquivo ZIP contendo todas as thumbnails de um vídeo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Thumbnails baixadas com sucesso"),
        ApiResponse(responseCode = "404", description = "Vídeo não encontrado")
    ])
    @GetMapping("/{id}/thumbnails/download")
    fun handleDownloadThumbnails(@PathVariable("id") id: Long): ResponseEntity<ByteArray> {
        val zip = runBlocking { retrieveVideoService.downloadThumbnails(id) }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_OCTET_STREAM
            set("Content-Disposition", "attachment; filename=\"images.zip\"")
        }

        return ResponseEntity.ok().headers(headers).body(zip)
    }
}
