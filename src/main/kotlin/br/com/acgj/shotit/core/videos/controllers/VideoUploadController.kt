package br.com.acgj.shotit.core.videos.controllers

import br.com.acgj.shotit.core.videos.services.SignVideoService
import br.com.acgj.shotit.core.videos.services.UploadVideoService
import br.com.acgj.shotit.core.videos.ports.SignVideoUploadRequest
import br.com.acgj.shotit.core.videos.ports.SignedVideoDTO
import br.com.acgj.shotit.core.videos.ports.UploadVideoRequest
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.videos.services.RetrieveVideoService
import br.com.acgj.shotit.core.videos.services.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Upload de Vídeos", description = "APIs de upload e processamento de vídeos")
@RestController
@RequestMapping("/api/videos")
class VideoUploadController(
    private val applicationUser: ApplicationUser,
    private val videoService: VideoService,
    private val uploadService: UploadVideoService,
    private val signService: SignVideoService
) {

    @Operation(summary = "Upload de vídeo", description = "Faz upload de um novo arquivo de vídeo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Vídeo enviado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PostMapping
    fun handleUploadVideo(request: UploadVideoRequest): ResponseEntity<Void> {
        val user = applicationUser.fromAuthorizedContext()
        val videos = request.toDomain(user)

        // TODO: Adicionar um DTO para o upload.
        val response = runBlocking { uploadService.upload(videos) }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Assinar upload de vídeo", description = "Gera URLs assinadas para upload de vídeo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "URLs assinadas geradas com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PostMapping("/sign")
    fun handleSignVideo(@RequestBody request: SignVideoUploadRequest): ResponseEntity<List<SignedVideoDTO>> {
        val user = applicationUser.fromAuthorizedContext()
        val videos = request.toDomain(user)
        val signs = runBlocking { signService.sign(videos) }
        val response = signs.map { SignedVideoDTO(it.video.id!!, it.sign, it.video.originalName!!) }
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Completar upload de vídeo", description = "Marca o upload de um vídeo como completo e inicia o processamento")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Upload completado com sucesso"),
        ApiResponse(responseCode = "404", description = "Vídeo não encontrado"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PostMapping("/{id}/complete")
    fun handleAckVideoCompleteUpload(@PathVariable("id") id: Long): ResponseEntity<Any> {
        videoService.sendUploadedVideoToQueue(id)
        return ResponseEntity.ok().build<Any>()
    }
}