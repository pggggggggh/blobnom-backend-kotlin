package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo
import xyz.blobnom.blobnomkotlin.room.dto.external.SolvedAcProblemInfo
import xyz.blobnom.blobnomkotlin.room.dto.external.SolvedAcSearchResponse

@Component
class BojProblemFetcher(
    val webClient: WebClient
) : ProblemFetcher {
    override val platform: Platform = Platform.BOJ

    override suspend fun fetch(
        query: String,
        num: Int
    ): List<ProblemInfo> {
        val problems = mutableListOf<ProblemInfo>()
        val problemIds = mutableSetOf<Int>()

        if (query.startsWith("problemset:")) {
            val ids = query.removePrefix("problemset:").split(",")
            for (id in ids) { // TODO: 병렬 처리로 바꾸기
                if (problems.size >= num) break

                try {
                    val item = webClient.get()
                        .uri("https://solved.ac/api/v3/problem/lookup?problemIds=$id")
                        .retrieve()
                        .awaitBody<List<SolvedAcProblemInfo>>().firstOrNull()

                    if (item != null && item.problemId !in problemIds) {
                        problems.add(ProblemInfo(item.problemId.toString(), item.level))
                        problemIds.add(item.problemId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            repeat(5) {
                if (problems.size >= num) return@repeat

                try {
                    val response = webClient.get()
                        .uri { builder ->
                            builder.scheme("https").host("solved.ac")
                                .path("/api/v3/search/problem")
                                .queryParam("query", query)
                                .queryParam("sort", "random")
                                .queryParam("page", 1)
                                .build()
                        }
                        .retrieve()
                        .awaitBody<SolvedAcSearchResponse>()

                    for (item in response.items) {
                        if (item.problemId !in problemIds) {
                            problems.add(ProblemInfo(item.problemId.toString(), item.level))
                            problemIds.add(item.problemId)
                        }
                        if (problems.size >= num) break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return problems
    }
}
