package xyz.blobnom.blobnomkotlin.member.dto

import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.Role

data class MemberSummary(
    val id: Long,
    val handle: String,
    val role: Role,
    val rating: Int?,
    val accounts: Map<Platform, String>
)

