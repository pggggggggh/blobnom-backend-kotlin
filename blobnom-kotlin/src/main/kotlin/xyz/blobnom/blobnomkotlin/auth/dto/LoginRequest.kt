package xyz.blobnom.blobnomkotlin.auth.dto

data class LoginRequest(
    val handle: String,
    val password: String,
)