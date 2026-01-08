package xyz.blobnom.blobnomkotlin.member.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import xyz.blobnom.blobnomkotlin.common.Platform

// TODO: Rename to PlatformAccount
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "ix_users_name", columnList = "name")
    ]
)
class PlatformUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column
    val platform: Platform,

    @Column(name = "name")
    var handle: String,


    ) : BaseTimeEntity() {
    companion object {
        fun add(platform: Platform, member: Member, handle: String) = PlatformUser(
            member = member,
            platform = platform,
            handle = handle
        )
    }

    fun rename(newHandle: String) {
        handle = newHandle
    }
}