package br.com.acgj.shotit.core.users.controllers

import br.com.acgj.shotit.core.infra.auth.ApplicationUser
import br.com.acgj.shotit.core.auth.gateways.S3AvatarUploadGateway
import br.com.acgj.shotit.core.users.ports.UserProfileDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Perfil do Usuário", description = "APIs de gerenciamento de perfil do usuário")
@RestController
@RequestMapping("/api/profile")
class UserProfileController(private val applicationUser: ApplicationUser, private val gateway: S3AvatarUploadGateway) {

    @Operation(summary = "Obter perfil do usuário", description = "Recupera as informações do perfil do usuário autenticado")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso"),
        ApiResponse(responseCode = "401", description = "Não autorizado")
    ])
    @GetMapping
    fun handleGetProfile(): ResponseEntity<UserProfileDTO> {
        val fromContext = applicationUser
            .fromContext()
        return   ResponseEntity.ok(UserProfileDTO(fromContext!!))
    }
}