package xyz.blobnom.blobnomkotlin.member.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("passwordToken")
class PasswordResetToken(
    @Id
    val token: String,

    val memberId: Long,

    @TimeToLive
    val ttl: Long
) {
    companion object {
        const val TTL = 30 * 60L

        fun from(token: String, memberId: Long) = PasswordResetToken(
            token = token,
            memberId = memberId,
            ttl = TTL
        )
    }
}