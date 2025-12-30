package xyz.blobnom.blobnomkotlin.member.presentation

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.blobnom.blobnomkotlin.auth.infra.CustomUserDetails
import xyz.blobnom.blobnomkotlin.member.app.MemberRegisterService
import xyz.blobnom.blobnomkotlin.member.app.MemberService
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.member.dto.RegisterRequest

@RestController
@RequestMapping("/members")
class MemberController(
    private val memberService: MemberService,
    private val memberRegisterService: MemberRegisterService
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
}