package xyz.blobnom.blobnomkotlin.auth.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.auth.app.port.PlatformBioFetcherPort
import xyz.blobnom.blobnomkotlin.auth.infra.external.BioFetcher
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode

@Component
class PlatformBioFetcherAdapter(
    fetchers: List<BioFetcher>
) : PlatformBioFetcherPort {
    private val fetcherMap = fetchers.associateBy { it.platform }

    override suspend fun fetchBios(platform: Platform, handle: String): List<String> =
        fetcherMap[platform]?.fetchBios(handle)
            ?: throw CustomException(ErrorCode.UNSUPPORTED_PLATFORM)
}
