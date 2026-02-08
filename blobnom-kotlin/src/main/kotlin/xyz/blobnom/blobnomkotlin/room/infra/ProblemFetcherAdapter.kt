package xyz.blobnom.blobnomkotlin.room.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.app.port.ProblemFetcherPort
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo
import xyz.blobnom.blobnomkotlin.room.infra.external.ProblemFetcher
import java.util.concurrent.TimeoutException

@Component
class ProblemFetcherAdapter(
    fetchers: List<ProblemFetcher>
) : ProblemFetcherPort {
    private val fetcherMap = fetchers.associateBy { it.platform }

    override suspend fun fetch(platform: Platform, query: String, num: Int): List<ProblemInfo> =
        try {
            val fetcher = fetcherMap[platform]
                ?: throw CustomException(ErrorCode.UNSUPPORTED_PLATFORM)
            fetcher.fetch(query, num)
        } catch (_: TimeoutException) {
            throw CustomException(ErrorCode.EXTERNAL_API_TIMEOUT)
        }
}