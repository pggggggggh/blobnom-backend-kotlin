package xyz.blobnom.blobnomkotlin.member.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.common.app.EmailSenderPort
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.PasswordResetToken
import xyz.blobnom.blobnomkotlin.member.domain.TemporaryPasswordGenerator
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.member.domain.repository.PasswordResetTokenRepository
import java.util.UUID

@Service
class PasswordResetService(
    private val memberRepository: MemberRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailSenderPort: EmailSenderPort,
    private val temporaryPasswordGenerator: TemporaryPasswordGenerator,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun createTokenAndSendEmail(email: String) {
        val member = memberRepository.findByEmail(email) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)
        val token = UUID.randomUUID().toString()

        val resetToken = PasswordResetToken.from(token, member.id!!)
        passwordResetTokenRepository.save(resetToken)

        val resetLink = "https://blobnom.xyz/auth/retrieve-password?token=$token"
        emailSenderPort.sendEmail(
            member.email, "Blobnom 비밀번호 초기화 링크입니다.",
            """
            ${member.handle}님,
            다음 링크로 접속하여 초기화해주세요. 임시 비밀번호로 로그인한 뒤에 반드시 비밀번호를 변경하기 바랍니다.
            $resetLink
        """.trimIndent()
        )
    }

    @Transactional
    suspend fun retrievePassword(tokenString: String): String {
        val token = passwordResetTokenRepository.findByIdOrNull(tokenString)
            ?: throw CustomException(ErrorCode.FAILED_TOKEN_VERIFICATION)

        val member =
            memberRepository.findByIdOrNull(token.memberId) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)
        val newPassword = temporaryPasswordGenerator.generate()
        member.password = passwordEncoder.encode(newPassword)
        memberRepository.save(member)
        passwordResetTokenRepository.delete(token)

        return newPassword
    }
}