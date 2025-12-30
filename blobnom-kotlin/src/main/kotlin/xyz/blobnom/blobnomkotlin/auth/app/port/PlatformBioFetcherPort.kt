package xyz.blobnom.blobnomkotlin.auth.app.port

import xyz.blobnom.blobnomkotlin.common.Platform

interface PlatformBioFetcherPort {
    suspend fun fetchBios(platform: Platform, handle: String): List<String>
}