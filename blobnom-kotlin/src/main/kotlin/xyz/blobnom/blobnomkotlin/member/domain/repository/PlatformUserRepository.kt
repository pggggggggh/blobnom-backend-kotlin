package xyz.blobnom.blobnomkotlin.member.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.member.domain.PlatformUser

@Repository
interface PlatformUserRepository : JpaRepository<PlatformUser, Long> {
    fun findByHandleAndPlatform(handle: String, platform: Platform): PlatformUser?
}