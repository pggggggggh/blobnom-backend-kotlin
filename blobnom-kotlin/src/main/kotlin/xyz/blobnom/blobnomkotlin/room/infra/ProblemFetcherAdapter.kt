package xyz.blobnom.blobnomkotlin.room.infra

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.app.port.ProblemFetcherPort
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo
import xyz.blobnom.blobnomkotlin.room.infra.external.BojProblemFetcher
import xyz.blobnom.blobnomkotlin.room.infra.external.CodeforcesProblemFetcher
import java.util.concurrent.TimeoutException

@Component
class ProblemFetcherAdapter(
    private val bojProblemFetcher: BojProblemFetcher,
    private val codeforcesProblemFetcher: CodeforcesProblemFetcher
) : ProblemFetcherPort {
    override suspend fun fetch(platform: Platform, query: String, num: Int): List<ProblemInfo> =
        try {
            // TODO: Adding a new Platform requires modifying this switch, OCP violation
            when (platform) {
                Platform.BOJ -> bojProblemFetcher.fetch(query, num)
                Platform.CODEFORCES -> codeforcesProblemFetcher.fetch(query, num)
            }
        } catch (_: TimeoutException) {
            throw CustomException(ErrorCode.EXTERNAL_API_TIMEOUT)
        }
}