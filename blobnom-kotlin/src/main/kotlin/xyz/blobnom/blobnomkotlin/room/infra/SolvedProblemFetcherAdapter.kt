package xyz.blobnom.blobnomkotlin.room.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.app.port.SolvedProblemsFetcherPort
import xyz.blobnom.blobnomkotlin.room.infra.external.SolvedProblemFetcher

@Component
class SolvedProblemFetcherAdapter(
    fetchers: List<SolvedProblemFetcher>
) : SolvedProblemsFetcherPort {
    private val fetcherMap = fetchers.associateBy { it.platform }

    override suspend fun fetchSolvedProblemIds(
        handle: String,
        platform: Platform,
        targetProblemIds: List<String>
    ): List<String> =
        fetcherMap[platform]?.fetchSolvedProblemIds(handle, targetProblemIds)
            ?: throw CustomException(ErrorCode.UNSUPPORTED_PLATFORM)
}
