package xyz.blobnom.blobnomkotlin.member.dto

import xyz.blobnom.blobnomkotlin.common.Platform


data class PlatformAccountRequest(
    val platform: Platform,
    val handle: String
)
