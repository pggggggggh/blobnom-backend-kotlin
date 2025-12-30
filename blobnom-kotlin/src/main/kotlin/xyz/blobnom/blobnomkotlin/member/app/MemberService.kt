package xyz.blobnom.blobnomkotlin.member.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary


@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun getMemberSummaryById(id: Long): MemberSummary {
        val member = memberRepository.findByIdOrNull(id) ?: throw RuntimeException("Member not found")
        return member.toMemberSummary()
    }
}