package xyz.blobnom.blobnomkotlin.auth.dto.external

data class CodeforcesUserResult(
    val handle: String,
    val firstName: String?,
    val lastName: String?
)