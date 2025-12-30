package xyz.blobnom.blobnomkotlin.member.domain.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.blobnom.blobnomkotlin.member.domain.Member

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    @EntityGraph(attributePaths = ["platformUsers"])
    fun findWithPlatformUsersByHandle(handle: String): Member?

    fun findByHandle(handle: String): Member?

    fun findByEmail(email: String): Member?
}