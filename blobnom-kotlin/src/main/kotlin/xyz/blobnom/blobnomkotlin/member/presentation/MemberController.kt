package xyz.blobnom.blobnomkotlin.member.presentation

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.blobnom.blobnomkotlin.auth.infra.CustomUserDetails
import xyz.blobnom.blobnomkotlin.member.app.MemberModifyService
import xyz.blobnom.blobnomkotlin.member.app.MemberRegisterService
import xyz.blobnom.blobnomkotlin.member.app.MemberService
import xyz.blobnom.blobnomkotlin.member.app.PasswordResetService
import xyz.blobnom.blobnomkotlin.member.dto.MemberModifyRequest
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.member.dto.PlatformAccountRequest
import xyz.blobnom.blobnomkotlin.member.dto.RegisterRequest
import xyz.blobnom.blobnomkotlin.member.dto.RetrievePasswordResponse

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberService: MemberService,
    private val memberRegisterService: MemberRegisterService,
    private val passwordResetService: PasswordResetService,
    private val memberModifyService: MemberModifyService
) {
    @GetMapping("/me")
    fun getMySummary(@AuthenticationPrincipal principal: CustomUserDetails): ResponseEntity<MemberSummary> {
        return ResponseEntity.ok(memberService.getMemberSummaryById(principal.memberId))
    }

    @PostMapping
    suspend fun register(@RequestBody request: RegisterRequest): ResponseEntity<Unit> {
        memberRegisterService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PatchMapping
    suspend fun modify(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @RequestBody request: MemberModifyRequest
    ): ResponseEntity<Unit> {
        memberModifyService.modify(principal.memberId, request)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PostMapping("/reset-password")
    suspend fun resetPassword(@RequestBody email: String): ResponseEntity<Unit> {
        passwordResetService.createTokenAndSendEmail(email)
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @PostMapping("/retrieve-password")
    suspend fun retrievePassword(@RequestParam token: String): ResponseEntity<RetrievePasswordResponse> {
        val newPassword = passwordResetService.retrievePassword(token)
        return ResponseEntity.ok(RetrievePasswordResponse(newPassword))
    }

    @PostMapping("/validate-platform-account")
    suspend fun validatePlatformAccount(@RequestBody request: PlatformAccountRequest): ResponseEntity<Unit> {
        memberRegisterService.validatePlatformAccount(request)
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}