package xyz.blobnom.blobnomkotlin.room.infra.external

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.external.SolvedAcSearchResponse

@Component
class BojSolvedProblemFetcher(
    private val webClient: WebClient
) : SolvedProblemFetcher {
    override val platform: Platform = Platform.BOJ

    override suspend fun fetchSolvedProblemIds(
        handle: String,
        targetProblemIds: List<String>
    ): List<String> = coroutineScope {
        val chunks = targetProblemIds.chunked(25)
        val deferredResults = chunks.map { batch ->
            async { fetchBatch(handle, batch) }
        }
        deferredResults.awaitAll().flatten()
    }

    private suspend fun fetchBatch(handle: String, problemIds: List<String>): List<String> {
        val idQuery = problemIds.joinToString("|") { "id:$it" }
        val query = "($idQuery) & @$handle"
        return try {
            val response = webClient.get()
                .uri("https://solved.ac/api/v3/search/problem?query={query}", query)
                .retrieve()
                .awaitBody<SolvedAcSearchResponse>()
            response.items.map { it.problemId.toString() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
