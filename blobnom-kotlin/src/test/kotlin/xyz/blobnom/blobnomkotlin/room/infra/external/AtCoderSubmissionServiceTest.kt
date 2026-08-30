package xyz.blobnom.blobnomkotlin.room.infra.external

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblem
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblemModel
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderSubmission
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AtCoderSubmissionServiceTest {
    @DisplayName("Paginates submissions and fetches accepted problems on every request")
    @Test
    fun paginatesAndFetchesAcceptedProblems() = runBlocking {
        val firstPage = (1L..500L).map { epoch ->
            submission(epoch, if (epoch % 2L == 0L) "AC" else "WA")
        }
        val secondPage = listOf(submission(600L, "AC"))
        val api = SubmissionApi { _, fromSecond ->
            when (fromSecond) {
                0L -> firstPage
                501L -> secondPage
                else -> emptyList()
            }
        }
        val service = AtCoderSubmissionService(api)

        val first = service.fetchAll("AtCoder_User")
        val second = service.fetchAll("atcoder_user")

        assertEquals(251, first.size)
        assertEquals(true, "problem_600" in first)
        assertEquals(first, second)
        assertEquals(listOf(0L, 501L, 0L, 501L), api.requestedCursors)
    }

    @DisplayName("Rejects an invalid AtCoder handle before calling the API")
    @Test
    fun rejectsInvalidHandle() = runBlocking {
        val api = SubmissionApi { _, _ -> emptyList() }
        val service = AtCoderSubmissionService(api)

        assertFailsWith<IllegalArgumentException> {
            service.fetchAll("invalid handle")
        }
        assertEquals(emptyList(), api.requestedCursors)
    }

    private fun submission(epochSecond: Long, result: String) = AtCoderSubmission(
        id = epochSecond,
        epochSecond = epochSecond,
        problemId = "problem_$epochSecond",
        contestId = "contest",
        userId = "AtCoder_User",
        result = result,
    )
}

private class SubmissionApi(
    private val submissions: (String, Long) -> List<AtCoderSubmission>,
) : AtCoderProblemsApi {
    val requestedCursors = mutableListOf<Long>()

    override suspend fun fetchProblems(): List<AtCoderProblem> = emptyList()

    override suspend fun fetchProblemModels(): Map<String, AtCoderProblemModel> = emptyMap()

    override suspend fun fetchSubmissions(handle: String, fromSecond: Long): List<AtCoderSubmission> {
        requestedCursors += fromSecond
        return submissions(handle, fromSecond)
    }
}
