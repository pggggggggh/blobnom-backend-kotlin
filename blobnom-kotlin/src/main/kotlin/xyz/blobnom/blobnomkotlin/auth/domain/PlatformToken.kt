package xyz.blobnom.blobnomkotlin.auth.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import java.time.ZonedDateTime

@Entity
@Table(name = "solvedac_tokens")
class PlatformToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val token: String,

    @Column
    var isUsed: Boolean = false,

    @Column
    val expiresAt: ZonedDateTime
) : BaseTimeEntity()