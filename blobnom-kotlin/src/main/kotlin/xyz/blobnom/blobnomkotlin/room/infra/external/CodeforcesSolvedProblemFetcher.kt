package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesResponse
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesSubmission

@Component
class CodeforcesSolvedProblemFetcher(
    private val webClient: WebClient
) : SolvedProblemFetcher {
    override val platform: Platform = Platform.CODEFORCES

    override suspend fun fetchSolvedProblemIds(
        handle: String,
        targetProblemIds: List<String>
    ): List<String> {
        return try {
            val response = webClient.get()
                .uri("https://codeforces.com/api/user.status?handle={handle}", handle)
                .retrieve()
                .awaitBody<CodeforcesResponse<CodeforcesSubmission>>()
            val targetSet = targetProblemIds.toSet()

            response.result
                .filter { it.verdict == "OK" }
                .mapNotNull { submission ->
                    val contestId = submission.problem.contestId
                    val index = submission.problem.index
                    if (contestId != null) "$contestId$index" else null
                }
                .filter { pid -> targetSet.contains(pid) }
                .distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
