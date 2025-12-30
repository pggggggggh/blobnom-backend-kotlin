package xyz.blobnom.blobnomkotlin.auth.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PlatformTokenRepository : JpaRepository<PlatformToken, Long> {
    fun findByToken(token: String): PlatformToken?
}