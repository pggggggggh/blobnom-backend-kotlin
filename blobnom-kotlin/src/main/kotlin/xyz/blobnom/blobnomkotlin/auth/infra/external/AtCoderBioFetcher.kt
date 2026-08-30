package xyz.blobnom.blobnomkotlin.auth.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode

@Component
class AtCoderBioFetcher(
    private val webClient: WebClient,
) : BioFetcher {
    override val platform: Platform = Platform.ATCODER

    override suspend fun fetchBios(handle: String): List<String> {
        if (!HANDLE_REGEX.matches(handle)) throw CustomException(ErrorCode.PLATFORM_USER_NOT_FOUND)

        return try {
            val html = webClient.get()
                .uri { builder ->
                    builder.scheme("https")
                        .host("atcoder.jp")
                        .path("/users/{handle}")
                        .queryParam("lang", "en")
                        .build(handle)
                }
                .retrieve()
                .awaitBody<String>()

            listOfNotNull(extractAffiliation(html))
        } catch (_: Exception) {
            throw CustomException(ErrorCode.PLATFORM_USER_NOT_FOUND)
        }
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[0-9A-Za-z_]+$")
        private val AFFILIATION_REGEX = Regex(
            """<th[^>]*>\s*Affiliation\s*</th>\s*<td[^>]*>(.*?)</td>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        private val HTML_TAG_REGEX = Regex("<[^>]+>")

        internal fun extractAffiliation(html: String): String? =
            AFFILIATION_REGEX.find(html)
                ?.groupValues
                ?.get(1)
                ?.replace(HTML_TAG_REGEX, "")
                ?.replace("&amp;", "&")
                ?.replace("&lt;", "<")
                ?.replace("&gt;", ">")
                ?.replace("&quot;", "\"")
                ?.replace("&#39;", "'")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
    }
}
