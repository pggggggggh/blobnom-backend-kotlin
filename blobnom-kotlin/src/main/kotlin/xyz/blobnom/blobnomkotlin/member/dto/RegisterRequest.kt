package xyz.blobnom.blobnomkotlin.member.dto


data class RegisterRequest(
    val handle: String,
    val email: String,
    val password: String,
    val platformAccountRequests: List<PlatformAccountRequest>
)