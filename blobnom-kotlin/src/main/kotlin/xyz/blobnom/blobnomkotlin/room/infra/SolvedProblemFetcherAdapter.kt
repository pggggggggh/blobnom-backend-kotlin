package xyz.blobnom.blobnomkotlin.room.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.app.port.SolvedProblemsFetcherPort
import xyz.blobnom.blobnomkotlin.room.infra.external.BojSolvedProblemsFetcher
import xyz.blobnom.blobnomkotlin.room.infra.external.CodeforcesSolvedProblemsFetcher

@Component
class SolvedProblemFetcherAdapter(
    private val bojSolvedProblemsFetcher: BojSolvedProblemsFetcher,
    private val codeforcesSolvedProblemsFetcher: CodeforcesSolvedProblemsFetcher
) : SolvedProblemsFetcherPort {
    override suspend fun fetchSolvedProblemIds(
        handle: String,
        platform: Platform,
        targetProblemIds: List<String>
    ): List<String> =
        when (platform) {
            // TODO: Adding a new Platform requires modifying this switch, OCP violation
            Platform.BOJ -> bojSolvedProblemsFetcher.fetch(handle, targetProblemIds)
            Platform.CODEFORCES -> codeforcesSolvedProblemsFetcher.fetch(handle, targetProblemIds)
        }
}