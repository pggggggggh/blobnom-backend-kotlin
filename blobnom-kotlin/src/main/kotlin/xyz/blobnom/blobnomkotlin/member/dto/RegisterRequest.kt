package xyz.blobnom.blobnomkotlin.member.dto

import xyz.blobnom.blobnomkotlin.common.Platform

data class PlatformUserRequest(
    val platform: Platform,
    val handle: String
)

data class RegisterRequest(
    val handle: String,
    val email: String,
    val password: String,
    val platformUserRequests: List<PlatformUserRequest>
)