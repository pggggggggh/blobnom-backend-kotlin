package xyz.blobnom.blobnomkotlin.auth.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.auth.dto.external.BojUserResponse
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode

@Component
class BojBioFetcher(
    private val webClient: WebClient
) : BioFetcher {
    override val platform: Platform = Platform.BOJ

    override suspend fun fetchBios(handle: String): List<String> {
        return try {
            val response = webClient.get()
                .uri("https://solved.ac/api/v3/user/show?handle={handle}", handle)
                .retrieve()
                .awaitBody<BojUserResponse>()
            listOfNotNull(response.bio)
        } catch (_: Exception) {
            throw CustomException(ErrorCode.PLATFORM_USER_NOT_FOUND)
        }
    }
}
