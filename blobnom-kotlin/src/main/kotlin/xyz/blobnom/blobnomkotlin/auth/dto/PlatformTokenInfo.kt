package xyz.blobnom.blobnomkotlin.auth.dto

import java.time.ZonedDateTime

data class PlatformTokenInfo(
    val token: String,
    val expiresAt: ZonedDateTime
)