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

        for (platformUserRequest in registerRequest.platformAccountRequests) {
            if (platformUserRepository.findByHandleAndPlatform(
                    platformUserRequest.handle,
                    platformUserRequest.platform
                ) != null
            )
                throw CustomException(ErrorCode.ALREADY_TAKEN)

            member.linkPlatform(platformUserRequest.platform, platformUserRequest.handle)
        }

        memberRepository.save(member)
    }

    @Transactional
    suspend fun validatePlatformAccount(request: PlatformAccountRequest): Boolean {
        if (platformUserRepository.findByHandleAndPlatform(
                request.handle,
                request.platform
            ) != null
        )
            throw CustomException(ErrorCode.ALREADY_TAKEN)

        authService.validatePlatformAccount(request.platform, request.handle)
        return true
    }
}