package br.com.acgj.shotit.core.auth.controllers

import br.com.acgj.shotit.core.auth.ports.SignInRequest
import br.com.acgj.shotit.core.auth.ports.SignUpRequest
import br.com.acgj.shotit.core.auth.services.AuthenticationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SuccessAuthentication(val token: String)

@Tag(name = "Autenticação", description = "APIs de gerenciamento de autenticação")
@RequestMapping("/api/auth")
@RestController
class AuthenticationController(private val service: AuthenticationService) {

    @Operation(summary = "Login de usuário", description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
        ApiResponse(responseCode = "400", description = "Credenciais inválidas")
    ])
    @PostMapping("/sign-in")
    fun handleSignIn(@RequestBody request: SignInRequest): ResponseEntity<SuccessAuthentication> {
        val token = service.authenticate(request)
        val response = SuccessAuthentication(token)

        return ResponseEntity.ok().body(response)
    }

    @Operation(summary = "Cadastro de usuário", description = "Registra um novo usuário no sistema")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Registrado com sucesso"),
        ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    ])
    @PostMapping("/sign-up")
    fun handleSignUp(request: SignUpRequest): ResponseEntity<Any>{
        service.register(request)
        return ResponseEntity.ok().build()
    }
}