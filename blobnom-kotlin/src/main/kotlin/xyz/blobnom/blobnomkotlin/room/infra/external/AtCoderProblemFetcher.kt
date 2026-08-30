package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblem
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblemModel
import java.util.Locale
import kotlin.math.exp
import kotlin.math.roundToInt

@Component
class AtCoderProblemFetcher(
    private val api: AtCoderProblemsApi,
    private val solvedProblems: AtCoderSolvedProblems,
) : ProblemFetcher {
    override val platform: Platform = Platform.ATCODER

    override suspend fun fetch(query: String, num: Int): List<ProblemInfo> {
        require(num >= 0) { "num must not be negative" }
        if (num == 0) return emptyList()

        val catalog = fetchCatalog()
        if (query.startsWith(PROBLEM_SET_PREFIX)) {
            return fetchProblemSet(query, num, catalog)
        }

        val parsedQuery = parseQuery(query)
        val forbiddenProblemIds = parsedQuery.excludedHandles
            .flatMap { solvedProblems.fetchAll(it) }
            .toSet()

        return catalog.problems.asSequence()
            .filter { problem -> parsedQuery.matches(problem, catalog.models[problem.id]) }
            .filterNot { it.id in forbiddenProblemIds }
            .map { problem ->
                ProblemInfo(
                    id = problem.id,
                    difficulty = displayDifficulty(catalog.models[problem.id]?.difficulty),
                )
            }
            .shuffled()
            .take(num)
            .toList()
    }

    private fun fetchProblemSet(query: String, num: Int, catalog: ProblemCatalog): List<ProblemInfo> {
        val problemIds = query.removePrefix(PROBLEM_SET_PREFIX)
            .substringBefore(' ')
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val problemByLowercaseId = catalog.problems.associateBy { it.id.lowercase(Locale.ROOT) }
        return problemIds.asSequence()
            .mapNotNull { problemByLowercaseId[it.lowercase(Locale.ROOT)] }
            .take(num)
            .map { problem ->
                ProblemInfo(
                    id = problem.id,
                    difficulty = displayDifficulty(catalog.models[problem.id]?.difficulty),
                )
            }
            .toList()
    }

    private suspend fun fetchCatalog(): ProblemCatalog = ProblemCatalog(
        problems = canonicalProblems(api.fetchProblems()),
        models = api.fetchProblemModels(),
    )

    private fun canonicalProblems(problems: List<AtCoderProblem>): List<AtCoderProblem> =
        problems.groupBy { it.id }.values.map { candidates ->
            candidates.minWith(
                compareBy<AtCoderProblem> { canonicalContestScore(it) }
                    .thenBy { it.contestId },
            )
        }

    private fun canonicalContestScore(problem: AtCoderProblem): Int {
        val expectedContestId = problem.id.substringBeforeLast('_', missingDelimiterValue = "")
        return when {
            problem.contestId.equals(expectedContestId, ignoreCase = true) -> 0
            !problem.contestId.startsWith("adt_", ignoreCase = true) -> 1
            else -> 2
        }
    }

    private fun parseQuery(query: String): AtCoderQuery {
        var difficultyRange: IntRange? = null
        var contestTypes: Set<String>? = null
        var contestIds: Set<String>? = null
        var problemIndexes: Set<String>? = null
        val excludedHandles = linkedSetOf<String>()

        try {
            query.split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .forEach { token ->
                    when {
                        token.startsWith(DIFFICULTY_PREFIX) -> {
                            val range = token.removePrefix(DIFFICULTY_PREFIX).split('-', limit = 2)
                            require(range.size == 2)
                            val start = range[0].toInt()
                            val end = range[1].toInt()
                            require(start <= end)
                            difficultyRange = start..end
                        }

                        token.startsWith(CONTEST_TYPE_PREFIX) -> {
                            contestTypes = token.removePrefix(CONTEST_TYPE_PREFIX)
                                .split('|')
                                .map { it.lowercase(Locale.ROOT) }
                                .filter { it.isNotBlank() }
                                .toSet()
                                .also { require(it.isNotEmpty()) }
                        }

                        token.startsWith(CONTEST_ID_PREFIX) -> {
                            contestIds = token.removePrefix(CONTEST_ID_PREFIX)
                                .split('|')
                                .map { it.lowercase(Locale.ROOT) }
                                .filter { it.isNotBlank() }
                                .toSet()
                                .also { require(it.isNotEmpty()) }
                        }

                        token.startsWith(PROBLEM_ID_PREFIX) -> {
                            problemIndexes = token.removePrefix(PROBLEM_ID_PREFIX)
                                .split('|')
                                .map { it.lowercase(Locale.ROOT) }
                                .filter { it.isNotBlank() }
                                .toSet()
                                .also { require(it.isNotEmpty()) }
                        }

                        token.startsWith(EXCLUDED_HANDLE_PREFIX) -> {
                            val handle = token.removePrefix(EXCLUDED_HANDLE_PREFIX)
                            require(ATCODER_HANDLE_REGEX.matches(handle))
                            excludedHandles += handle
                        }
                    }
                }
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid query")
        }

        return AtCoderQuery(
            difficultyRange = difficultyRange,
            contestTypes = contestTypes,
            contestIds = contestIds,
            problemIndexes = problemIndexes,
            excludedHandles = excludedHandles,
        )
    }

    private fun displayDifficulty(difficulty: Double?): Int {
        if (difficulty == null) return UNKNOWN_DIFFICULTY
        return if (difficulty >= DIFFICULTY_CLIP_THRESHOLD) {
            difficulty.roundToInt()
        } else {
            (DIFFICULTY_CLIP_THRESHOLD /
                exp(1.0 - difficulty / DIFFICULTY_CLIP_THRESHOLD)).roundToInt()
        }
    }

    private data class ProblemCatalog(
        val problems: List<AtCoderProblem>,
        val models: Map<String, AtCoderProblemModel>,
    )

    private data class AtCoderQuery(
        val difficultyRange: IntRange?,
        val contestTypes: Set<String>?,
        val contestIds: Set<String>?,
        val problemIndexes: Set<String>?,
        val excludedHandles: Set<String>,
    ) {
        fun matches(problem: AtCoderProblem, model: AtCoderProblemModel?): Boolean {
            val displayedDifficulty = model?.difficulty?.let {
                if (it >= DIFFICULTY_CLIP_THRESHOLD) {
                    it.roundToInt()
                } else {
                    (DIFFICULTY_CLIP_THRESHOLD /
                        exp(1.0 - it / DIFFICULTY_CLIP_THRESHOLD)).roundToInt()
                }
            }
            if (difficultyRange != null && displayedDifficulty !in difficultyRange) return false

            val contestType = contestType(problem)
            if (contestTypes != null && contestType !in contestTypes) return false
            if (contestIds != null && problem.contestId.lowercase(Locale.ROOT) !in contestIds) return false
            if (problemIndexes != null && problem.problemIndex.lowercase(Locale.ROOT) !in problemIndexes) return false
            return true
        }

        private fun contestType(problem: AtCoderProblem): String {
            val taskPrefix = problem.id.substringBefore('_').lowercase(Locale.ROOT)
            return when {
                ABC_CONTEST_REGEX.matches(taskPrefix) -> "abc"
                ARC_CONTEST_REGEX.matches(taskPrefix) -> "arc"
                AGC_CONTEST_REGEX.matches(taskPrefix) -> "agc"
                AHC_CONTEST_REGEX.matches(taskPrefix) -> "ahc"
                else -> "etc"
            }
        }
    }

    private companion object {
        const val PROBLEM_SET_PREFIX = "problemset:"
        const val DIFFICULTY_PREFIX = "difficulty:"
        const val CONTEST_TYPE_PREFIX = "contesttype:"
        const val CONTEST_ID_PREFIX = "contestid:"
        const val PROBLEM_ID_PREFIX = "problemid:"
        const val EXCLUDED_HANDLE_PREFIX = "!@"
        const val UNKNOWN_DIFFICULTY = 0
        const val DIFFICULTY_CLIP_THRESHOLD = 400.0
        val ATCODER_HANDLE_REGEX = Regex("^[0-9A-Za-z_]+$")
        val ABC_CONTEST_REGEX = Regex("^abc\\d+$")
        val ARC_CONTEST_REGEX = Regex("^arc\\d+$")
        val AGC_CONTEST_REGEX = Regex("^agc\\d+$")
        val AHC_CONTEST_REGEX = Regex("^ahc\\d+$")
    }
}
