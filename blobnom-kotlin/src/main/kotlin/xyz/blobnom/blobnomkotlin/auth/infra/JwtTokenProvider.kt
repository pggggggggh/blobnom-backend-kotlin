package xyz.blobnom.blobnomkotlin.auth.infra

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Role
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-time}") private val expirationTime: Long,
) {
    private val key: SecretKey

    init {
        val keyBytes = Decoders.BASE64.decode(secret)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createToken(authentication: Authentication): String {
        val userDetails =
            authentication.principal as? CustomUserDetails ?: throw RuntimeException("Invalid user principal")
        val authorities = "ROLE_${userDetails.role}"

        val now = Date()
        val expiration = Date(now.time + expirationTime)

        return Jwts.builder().apply {
            subject(authentication.name)
            claim("authorities", authorities)
            claim("memberId", userDetails.memberId)
            issuedAt(now)
            expiration(expiration)
            signWith(key, Jwts.SIG.HS256)
        }.compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getClaims(token)
        val memberId = claims["memberId"].toString().toLongOrNull() ?: throw RuntimeException()
        val authorities: Collection<GrantedAuthority> = (claims["authorities"] ?: throw RuntimeException()).toString()
            .split(",")
            .map(::SimpleGrantedAuthority)
        val principal =
            CustomUserDetails(
                memberId = memberId,
                handle = claims.subject,
                hashedPassword = "",
                role = Role.valueOf(authorities.firstOrNull()?.toString()?.substring(5) ?: "MEMBER")
            )
        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    private fun getClaims(token: String): Claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload
}