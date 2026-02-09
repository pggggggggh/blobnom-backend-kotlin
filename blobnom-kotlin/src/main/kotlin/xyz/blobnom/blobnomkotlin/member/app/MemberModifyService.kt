package xyz.blobnom.blobnomkotlin.member.app

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.member.domain.repository.PlatformUserRepository
import xyz.blobnom.blobnomkotlin.member.dto.MemberModifyRequest

@Service
class MemberModifyService(
    private val memberRepository: MemberRepository,
    private val authService: AuthService,
    private val platformUserRepository: PlatformUserRepository,
) {
    @Transactional
    suspend fun modify(memberId: Long, request: MemberModifyRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)
        if (!authService.matchPassword(request.oldPassword, member.password))
            throw CustomException(ErrorCode.INVALID_PASSWORD)

        if (request.newPassword != null) {
            val hashedPassword = authService.hashPassword(request.newPassword)
            member.password = hashedPassword
        }

        val distinctCount = request.platformAccountRequests.distinctBy { it.platform }.size
        if (distinctCount != request.platformAccountRequests.size) throw CustomException(ErrorCode.DUPLICATED_PLATFORM_LINK)

        for (platformAccountRequest in request.platformAccountRequests) {
            val existingPlatformAccount = platformUserRepository.findByHandleAndPlatform(
                platformAccountRequest.handle,
                platformAccountRequest.platform
            )
            if (existingPlatformAccount != null) {
                if (existingPlatformAccount.member == null) existingPlatformAccount.member = member
                else if (existingPlatformAccount.member != member)
                    throw CustomException(ErrorCode.ALREADY_TAKEN)
                // else : skip already linked accounts
            } else {
                member.linkPlatform(platformAccountRequest.platform, platformAccountRequest.handle)
            }
        }

        memberRepository.save(member)
    }
}