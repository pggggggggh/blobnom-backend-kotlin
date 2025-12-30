package xyz.blobnom.blobnomkotlin.auth.infra

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.auth.app.port.SecurityPort

@Component
class SpringSecurityAdapter(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) : SecurityPort {
    override fun authenticate(handle: String, password: String): String {
        val token = UsernamePasswordAuthenticationToken(handle, password)
        val authentication = authenticationManager.authenticate(token)

        return jwtTokenProvider.createToken(authentication)
    }

    override fun hashPassword(rawPassword: String): String = passwordEncoder.encode(rawPassword)
    override fun matchPassword(rawPassword: String, hashedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, hashedPassword)
}