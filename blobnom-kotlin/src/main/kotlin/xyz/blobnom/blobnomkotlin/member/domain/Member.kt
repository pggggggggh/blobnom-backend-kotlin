package xyz.blobnom.blobnomkotlin.member.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.Role

@Entity
@Table(name = "members")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val email: String,

    @Column
    var handle: String,

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val platformUsers: MutableList<PlatformUser> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    var role: Role,

    @Column
    var password: String,

    @Column
    var rating: Int,
) : BaseTimeEntity() {
    companion object {
        fun create(handle: String, email: String, hashedPassword: String): Member {
            require(handle.isNotBlank()) { "핸들은 비어있을 수 없습니다." }
            require(email.contains("@")) { "이메일 형식이 올바르지 않습니다." }

            return Member(
                handle = handle,
                email = email,
                password = hashedPassword,
                role = Role.MEMBER,
                rating = 1200
            )
        }
    }

    fun linkPlatform(platform: Platform, handle: String) {
        val newLink = PlatformUser(
            member = this,
            platform = platform,
            handle = handle
        )
        this.platformUsers.add(newLink)
    }
}