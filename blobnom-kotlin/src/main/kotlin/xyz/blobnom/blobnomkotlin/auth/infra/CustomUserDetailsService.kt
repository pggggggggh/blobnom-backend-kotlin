package xyz.blobnom.blobnomkotlin.auth.infra

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository

@Service
class CustomUserDetailsService(private val memberRepository: MemberRepository) : UserDetailsService {
    override fun loadUserByUsername(handle: String): UserDetails? {
        val member =
            memberRepository.findByHandle(handle.lowercase()) ?: throw UsernameNotFoundException("User not found")
        return CustomUserDetails(
            memberId = member.id!!,
            handle = member.handle,
            hashedPassword = member.password,
            role = member.role
        )
    }
}