package xyz.blobnom.blobnomkotlin.member.dto

data class MemberModifyRequest(
    val oldPassword: String,
    val newPassword: String?,
    val platformAccountRequests: List<PlatformAccountRequest>,
)