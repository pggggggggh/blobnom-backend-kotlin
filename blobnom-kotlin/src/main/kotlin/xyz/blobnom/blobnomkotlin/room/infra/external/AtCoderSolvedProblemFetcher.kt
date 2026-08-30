package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform

@Component
class AtCoderSolvedProblemFetcher(
    private val solvedProblems: AtCoderSolvedProblems,
) : SolvedProblemFetcher {
    override val platform: Platform = Platform.ATCODER

    override suspend fun fetchSolvedProblemIds(
        handle: String,
        targetProblemIds: List<String>,
    ): List<String> {
        if (targetProblemIds.isEmpty()) return emptyList()

        return try {
            val targetSet = targetProblemIds.toSet()
            solvedProblems.fetchAll(handle).filter { it in targetSet }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
