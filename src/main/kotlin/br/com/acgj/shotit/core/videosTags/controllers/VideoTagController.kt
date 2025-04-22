package br.com.acgj.shotit.core.videosTags.controllers

import br.com.acgj.shotit.core.domain.VideoCategoryTagRepository
import br.com.acgj.shotit.core.videosTags.ports.TagDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Tags", description = "APIs de gerenciamento de tags de vídeos")
@RestController
@RequestMapping("/api/tags")
class VideoTagController(private val repository: VideoCategoryTagRepository) {
    
    @Operation(summary = "Listar todas as tags", description = "Recupera todas as tags de categorias de vídeos disponíveis")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Tags recuperadas com sucesso")
    ])
    @GetMapping
    fun handleRetrieveTags() = ResponseEntity.ok( repository.findAll().map { TagDTO(it.id!!, it.name) } )
}