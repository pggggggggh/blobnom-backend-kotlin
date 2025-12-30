package xyz.blobnom.blobnomkotlin.auth.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.auth.app.port.PlatformBioFetcherPort
import xyz.blobnom.blobnomkotlin.auth.infra.external.BojBioFetcher
import xyz.blobnom.blobnomkotlin.auth.infra.external.CodeforcesBioFetcher
import xyz.blobnom.blobnomkotlin.common.Platform

@Component
class PlatformBioFetcherAdapter(
    private val bojFetcher: BojBioFetcher,
    private val cfFetcher: CodeforcesBioFetcher
) : PlatformBioFetcherPort {
    override suspend fun fetchBios(platform: Platform, handle: String): List<String> =
        when (platform) {
            Platform.BOJ -> bojFetcher.fetchBios(handle)
            Platform.CODEFORCES -> cfFetcher.fetchBios(handle)
        }
}