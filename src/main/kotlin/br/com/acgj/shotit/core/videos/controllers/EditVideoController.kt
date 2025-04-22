package br.com.acgj.shotit.core.videos.controllers

import br.com.acgj.shotit.core.domain.BadRequestError
import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.videos.ports.UpdateThumbnailFavorite
import br.com.acgj.shotit.core.videos.ports.UpdateVideoTags
import br.com.acgj.shotit.core.videos.ports.UpdateVideoTitleRequest
import br.com.acgj.shotit.core.videos.services.EditVideoService
import br.com.acgj.shotit.core.videos.services.RetrieveVideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Edição de Vídeos", description = "APIs de edição e gerenciamento de vídeos")
@RestController
@RequestMapping("/api/videos")
class EditVideoController(
    private val applicationUser: ApplicationUser,
    private val retrieveVideoService: RetrieveVideoService,
    private val editVideoService: EditVideoService
){
    @Operation(summary = "Definir thumbnail favorita", description = "Define uma thumbnail específica como favorita para um vídeo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Thumbnail favorita atualizada com sucesso"),
        ApiResponse(responseCode = "404", description = "Vídeo ou thumbnail não encontrada"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PatchMapping("/{id}/thumbnails/favorite")
    fun handleFavoriteThumbnail(@PathVariable("id") id: Long, @RequestBody request: UpdateThumbnailFavorite){
        editVideoService.changeThumbnail(request.thumbnailId)
    }

    @Operation(summary = "Atualizar título do vídeo", description = "Atualiza o título de um vídeo específico")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Título do vídeo atualizado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PatchMapping("/{id}/change-name")
    fun handleChangeVideoName(@PathVariable("id") id: Long, @RequestBody request: UpdateVideoTitleRequest): ResponseEntity<Any> {
        val user = applicationUser.fromContext()!!

        val updated = editVideoService.updateVideoTitle(user, id, request.title)

        if (!updated){
            throw BadRequestError("Invalid Request", "video_not_found", "There is no video for this user")
        }

        return ResponseEntity.ok().build<Any>()
    }

    @Operation(summary = "Atualizar tags do vídeo", description = "Atualiza as tags associadas a um vídeo")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Tags do vídeo atualizadas com sucesso"),
        ApiResponse(responseCode = "404", description = "Vídeo não encontrado"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @PatchMapping("/{id}/tags")
    fun handleChangeVideoTags(@PathVariable("id") id: Long, @RequestBody request: UpdateVideoTags): ResponseEntity<Any> {
        editVideoService.updateVideoTags(id, request.tagIds)

        return ResponseEntity.ok().build()
    }
}
