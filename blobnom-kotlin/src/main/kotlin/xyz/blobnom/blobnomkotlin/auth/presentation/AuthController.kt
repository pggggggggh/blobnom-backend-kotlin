package xyz.blobnom.blobnomkotlin.auth.presentation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.blobnom.blobnomkotlin.auth.dto.LoginRequest
import xyz.blobnom.blobnomkotlin.auth.dto.JwtTokenInfo
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.auth.dto.PlatformTokenInfo

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<JwtTokenInfo> {
        val tokenInfo = authService.login(request)
        return ResponseEntity.ok(tokenInfo)
    }

    @GetMapping("/solvedac_token")
    fun createPlatformToken(): ResponseEntity<PlatformTokenInfo> {
        val platformTokenInfo = authService.createPlatformToken()
        return ResponseEntity.ok(platformTokenInfo)
    }
}