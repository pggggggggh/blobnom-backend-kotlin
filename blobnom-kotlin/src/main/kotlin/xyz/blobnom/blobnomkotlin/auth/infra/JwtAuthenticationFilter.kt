package xyz.blobnom.blobnomkotlin.auth.infra

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val token = resolveToken(req)
        if (token != null) {
            try {
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                sendErrorResponse(res, e)
                return
            }
        }
        chain.doFilter(req, res)
    }

    private fun sendErrorResponse(res: HttpServletResponse, e: Exception) {
        res.status = HttpServletResponse.SC_UNAUTHORIZED
        res.characterEncoding = "UTF-8"
        res.contentType = "application/json"

        res.writer.write("Error: " + e.message)
    }

    private fun resolveToken(req: HttpServletRequest): String? {
        return req.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer") }
            ?.substring(7)
    }
}