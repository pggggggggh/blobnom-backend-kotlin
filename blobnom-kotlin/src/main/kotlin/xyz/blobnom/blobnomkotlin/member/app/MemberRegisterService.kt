package xyz.blobnom.blobnomkotlin.member.app

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.member.domain.repository.PlatformUserRepository
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

        val hashedPassword = authService.hashPassword(registerRequest.password)
        val member = Member.create(
            handle = registerRequest.handle.lowercase(),
            email = registerRequest.email.lowercase(),
            hashedPassword = hashedPassword,
        )

        for (platformUserRequest in registerRequest.platformUserRequests) {
            if (platformUserRepository.findByHandleAndPlatform(
                    platformUserRequest.handle,
                    platformUserRequest.platform
                ) != null
            )
                throw CustomException(ErrorCode.ALREADY_TAKEN)

            authService.validatePlatformAccount(platformUserRequest.platform, platformUserRequest.handle)
            member.linkPlatform(platformUserRequest.platform, platformUserRequest.handle)
        }

        memberRepository.save(member)
    }
}