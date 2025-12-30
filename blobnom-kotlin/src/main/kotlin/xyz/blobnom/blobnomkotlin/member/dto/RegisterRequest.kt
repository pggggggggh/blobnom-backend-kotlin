package xyz.blobnom.blobnomkotlin.member.dto

import xyz.blobnom.blobnomkotlin.common.Platform

data class RegisterRequest(
    val platform: Platform,
    val handle: String,
    val email: String,
    val password: String
)