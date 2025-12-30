package xyz.blobnom.blobnomkotlin.auth.infra

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import xyz.blobnom.blobnomkotlin.common.Role

class CustomUserDetails(
    val memberId: Long,
    val handle: String,
    val hashedPassword: String,
    val role: Role
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    override fun getPassword(): String? {
        return hashedPassword
    }

    override fun getUsername(): String {
        return handle
    }

}