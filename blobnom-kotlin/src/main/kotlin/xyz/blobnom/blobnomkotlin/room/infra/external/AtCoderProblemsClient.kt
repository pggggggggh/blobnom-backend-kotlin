package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblem
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblemModel
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderSubmission

@Component
class AtCoderProblemsClient(
    private val webClient: WebClient,
) : AtCoderProblemsApi {
    override suspend fun fetchProblems(): List<AtCoderProblem> =
        webClient.get()
            .uri("https://kenkoooo.com/atcoder/resources/problems.json")
            .retrieve()
            .awaitBody()

    override suspend fun fetchProblemModels(): Map<String, AtCoderProblemModel> =
        webClient.get()
            .uri("https://kenkoooo.com/atcoder/resources/problem-models.json")
            .retrieve()
            .awaitBody()

    override suspend fun fetchSubmissions(handle: String, fromSecond: Long): List<AtCoderSubmission> =
        webClient.get()
            .uri { builder ->
                builder.scheme("https")
                    .host("kenkoooo.com")
                    .path("/atcoder/atcoder-api/v3/user/submissions")
                    .queryParam("user", handle)
                    .queryParam("from_second", fromSecond)
                    .build()
            }
            .retrieve()
            .awaitBody()
}
