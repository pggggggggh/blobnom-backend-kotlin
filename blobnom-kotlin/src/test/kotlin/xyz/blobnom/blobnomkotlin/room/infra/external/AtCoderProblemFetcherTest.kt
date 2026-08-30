package xyz.blobnom.blobnomkotlin.room.infra.external

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblem
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblemModel
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderSubmission
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AtCoderProblemFetcherTest {
    @DisplayName("Filters problems by difficulty, contest type, problem index, and solved status")
    @Test
    fun filtersProblemsByQuery() = runBlocking {
        val api = FakeAtCoderProblemsApi(
            problems = listOf(
                problem("abc100_a", "abc100", "A"),
                problem("abc100_b", "abc100", "B"),
                problem("arc100_a", "arc100", "A"),
            ),
            models = mapOf(
                "abc100_a" to AtCoderProblemModel(difficulty = 800.0),
                "abc100_b" to AtCoderProblemModel(difficulty = 850.0),
                "arc100_a" to AtCoderProblemModel(difficulty = 800.0),
            ),
        )
        val solvedProblems = FakeAtCoderSolvedProblems(mapOf("alice" to setOf("abc100_b")))
        val fetcher = AtCoderProblemFetcher(api, solvedProblems)

        val result = fetcher.fetch(
            "difficulty:700-900 contesttype:abc problemid:a|b !@alice",
            10,
        )

        assertEquals(listOf("abc100_a"), result.map { it.id })
        assertEquals(800, result.single().difficulty)
    }

    @DisplayName("Uses the canonical contest and fetches a fresh problem catalog")
    @Test
    fun usesCanonicalContestAndFreshCatalog() = runBlocking {
        val api = FakeAtCoderProblemsApi(
            problems = listOf(
                problem("abc350_a", "adt_all_20260701_1", "A"),
                problem("abc350_a", "abc350", "A"),
            ),
            models = mapOf("abc350_a" to AtCoderProblemModel(difficulty = 100.0)),
        )
        val fetcher = AtCoderProblemFetcher(api, FakeAtCoderSolvedProblems())

        val first = fetcher.fetch("contestid:abc350", 1)
        val second = fetcher.fetch("contestid:abc350", 1)

        assertEquals("abc350_a", first.single().id)
        assertEquals("abc350_a", second.single().id)
        assertEquals(189, first.single().difficulty)
        assertEquals(2, api.problemRequestCount)
        assertEquals(2, api.modelRequestCount)
    }

    @DisplayName("Preserves problem set order and uses zero for unknown difficulty")
    @Test
    fun preservesProblemSetOrder() = runBlocking {
        val api = FakeAtCoderProblemsApi(
            problems = listOf(
                problem("abc100_a", "abc100", "A"),
                problem("abc100_b", "abc100", "B"),
            ),
            models = mapOf("abc100_a" to AtCoderProblemModel(difficulty = 400.0)),
        )
        val fetcher = AtCoderProblemFetcher(api, FakeAtCoderSolvedProblems())

        val result = fetcher.fetch("problemset:ABC100_B,abc100_a", 2)

        assertEquals(listOf("abc100_b", "abc100_a"), result.map { it.id })
        assertEquals(listOf(0, 400), result.map { it.difficulty })
    }

    @DisplayName("Rejects a malformed query")
    @Test
    fun rejectsMalformedQuery() {
        runBlocking {
            val fetcher = AtCoderProblemFetcher(
                FakeAtCoderProblemsApi(emptyList(), emptyMap()),
                FakeAtCoderSolvedProblems(),
            )

            assertFailsWith<IllegalArgumentException> {
                fetcher.fetch("difficulty:900-400", 1)
            }
        }
    }

    private fun problem(id: String, contestId: String, index: String) = AtCoderProblem(
        id = id,
        contestId = contestId,
        problemIndex = index,
        name = id,
        title = "$index. $id",
    )
}

private class FakeAtCoderProblemsApi(
    private val problems: List<AtCoderProblem>,
    private val models: Map<String, AtCoderProblemModel>,
    private val submissions: (String, Long) -> List<AtCoderSubmission> = { _, _ -> emptyList() },
) : AtCoderProblemsApi {
    var problemRequestCount = 0
    var modelRequestCount = 0

    override suspend fun fetchProblems(): List<AtCoderProblem> {
        problemRequestCount++
        return problems
    }

    override suspend fun fetchProblemModels(): Map<String, AtCoderProblemModel> {
        modelRequestCount++
        return models
    }

    override suspend fun fetchSubmissions(handle: String, fromSecond: Long): List<AtCoderSubmission> =
        submissions(handle, fromSecond)
}

private class FakeAtCoderSolvedProblems(
    private val solvedByHandle: Map<String, Set<String>> = emptyMap(),
) : AtCoderSolvedProblems {
    override suspend fun fetchAll(handle: String): Set<String> = solvedByHandle[handle].orEmpty()
}
