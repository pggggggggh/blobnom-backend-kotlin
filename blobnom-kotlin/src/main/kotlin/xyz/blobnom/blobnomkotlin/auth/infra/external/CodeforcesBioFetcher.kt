package xyz.blobnom.blobnomkotlin.auth.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.auth.dto.external.CodeforcesUserResponse
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode

@Component
class CodeforcesBioFetcher(
    private val webClient: WebClient
) {
    suspend fun fetchBios(handle: String): List<String> {
        try {
            val response = webClient.get()
                .uri("https://codeforces.com/api/user.info?handles={handle}", handle)
                .retrieve()
                .awaitBody<CodeforcesUserResponse>()
            if (response.status != "OK" || response.result.isNullOrEmpty()) {
                throw CustomException(ErrorCode.PLATFORM_USER_NOT_FOUND)
            }
            val user = response.result[0]

            return listOfNotNull(user.firstName, user.lastName)
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            throw CustomException(ErrorCode.PLATFORM_USER_NOT_FOUND)
        }
    }
}