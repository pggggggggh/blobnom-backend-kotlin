package xyz.blobnom.blobnomkotlin.member.app

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.member.domain.repository.PlatformUserRepository
import xyz.blobnom.blobnomkotlin.member.dto.PlatformAccountRequest
import xyz.blobnom.blobnomkotlin.member.dto.RegisterRequest

@Service
class MemberRegisterService(
    private val authService: AuthService,
    private val memberRepository: MemberRepository,
    private val platformUserRepository: PlatformUserRepository,
) {
    @Transactional
    suspend fun register(registerRequest: RegisterRequest) {
        if (memberRepository.findByEmail(registerRequest.email) != null)
            throw CustomException(ErrorCode.ALREADY_TAKEN)
        if (memberRepository.findByEmail(registerRequest.handle) != null)
            throw CustomException(ErrorCode.ALREADY_TAKEN)


        val hashedPassword = authService.hashPassword(registerRequest.password)
        val member = Member.create(
            handle = registerRequest.handle.lowercase(),
            email = registerRequest.email.lowercase(),
            hashedPassword = hashedPassword,
        )

        // check if there is duplicated platform
        val distinctCount = registerRequest.platformAccountRequests.distinctBy { it.platform }.size
        if (distinctCount != registerRequest.platformAccountRequests.size) throw CustomException(ErrorCode.DUPLICATED_PLATFORM_LINK)

        for (platformAccountRequest in registerRequest.platformAccountRequests) {
            val existingPlatformAccount = platformUserRepository.findByHandleAndPlatform(
                platformAccountRequest.handle,
                platformAccountRequest.platform
            )
            if (existingPlatformAccount?.member != null)
                throw CustomException(ErrorCode.ALREADY_TAKEN)
            else if (existingPlatformAccount != null) existingPlatformAccount.member = member
            else member.linkPlatform(platformAccountRequest.platform, platformAccountRequest.handle)
        }

        memberRepository.save(member)
    }

    @Transactional
    suspend fun validatePlatformAccount(request: PlatformAccountRequest): Boolean {
        val existingPlatformAccount = platformUserRepository.findByHandleAndPlatform(
            request.handle,
            request.platform
        )
        if (existingPlatformAccount != null) {
            if (existingPlatformAccount.member != null)
                throw CustomException(ErrorCode.ALREADY_TAKEN)
        }

        authService.validatePlatformAccount(request.platform, request.handle)
        return true
    }
}