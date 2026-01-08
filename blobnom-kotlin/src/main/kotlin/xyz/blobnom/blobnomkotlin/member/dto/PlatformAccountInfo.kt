package xyz.blobnom.blobnomkotlin.member.dto

import xyz.blobnom.blobnomkotlin.common.Platform

data class PlatformAccountInfo(
    val platform: Platform,
    val handle: String,
)
