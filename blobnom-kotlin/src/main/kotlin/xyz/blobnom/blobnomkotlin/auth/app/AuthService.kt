package xyz.blobnom.blobnomkotlin.auth.app

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.auth.app.port.SecurityPort
import xyz.blobnom.blobnomkotlin.auth.app.port.PlatformBioFetcherPort
import xyz.blobnom.blobnomkotlin.auth.domain.PlatformToken
import xyz.blobnom.blobnomkotlin.auth.domain.PlatformTokenRepository
import xyz.blobnom.blobnomkotlin.auth.dto.LoginRequest
import xyz.blobnom.blobnomkotlin.auth.dto.JwtTokenInfo
import xyz.blobnom.blobnomkotlin.auth.dto.PlatformTokenInfo
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import java.time.ZonedDateTime
import java.util.UUID

@Service
class AuthService(
    private val securityPort: SecurityPort,
    private val platformTokenRepository: PlatformTokenRepository,
    private val platformBioFetcherPort: PlatformBioFetcherPort
) {
    fun login(request: LoginRequest): JwtTokenInfo {
        val token = try {
            securityPort.authenticate(request.handle, request.password)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.BAD_CREDENTIALS, e)
        }
        return JwtTokenInfo(token)
    }

    fun hashPassword(rawPassword: String): String = securityPort.hashPassword(rawPassword)

    fun matchPassword(rawPassword: String, hashedPassword: String): Boolean =
        securityPort.matchPassword(rawPassword, hashedPassword)

    @Transactional
    fun createPlatformToken(): PlatformTokenInfo {
        val token = UUID.randomUUID().toString()
        val platformToken = PlatformToken(
            token = token,
            expiresAt = ZonedDateTime.now().plusMinutes(10),
        )
        platformTokenRepository.save(platformToken)
        return PlatformTokenInfo(
            token = token,
            expiresAt = platformToken.expiresAt
        )
    }

    @Transactional
    suspend fun validatePlatformAccount(platform: Platform, handle: String) {
        val bios = platformBioFetcherPort.fetchBios(platform, handle)
        if (!bios.any { platformTokenRepository.findByToken(it) != null }) throw CustomException(ErrorCode.FAILED_TOKEN_VERIFICATION)
    }
}