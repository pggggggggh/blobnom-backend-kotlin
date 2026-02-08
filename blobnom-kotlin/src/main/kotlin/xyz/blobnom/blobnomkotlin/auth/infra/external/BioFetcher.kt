package xyz.blobnom.blobnomkotlin.auth.infra.external

import xyz.blobnom.blobnomkotlin.common.Platform

interface BioFetcher {
    val platform: Platform
    suspend fun fetchBios(handle: String): List<String>
}