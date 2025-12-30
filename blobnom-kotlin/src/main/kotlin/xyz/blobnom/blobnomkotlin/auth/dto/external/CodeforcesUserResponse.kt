package xyz.blobnom.blobnomkotlin.auth.dto.external

data class CodeforcesUserResponse(
    val status: String,
    val result: List<CodeforcesUserResult>?
)