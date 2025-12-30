package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesContest
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesProblemSetResponse
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesResponse
import xyz.blobnom.blobnomkotlin.room.dto.external.CodeforcesSubmission

@Component
class CodeforcesProblemFetcher(
    private val webClient: WebClient
) {
    suspend fun fetch(query: String, num: Int): List<ProblemInfo> { // TODO: Refactor & Asynchronous fetching
        var diffS: Int? = null
        var diffE: Int? = null
        var cidS: Int? = null
        var cidE: Int? = null
        var cidOnlyOdd = false
        var ctypes = listOf("div1", "div2", "global", "div3", "div4", "edu", "etc")
        var pids = setOf<String>()
        val forbiddenHandles = mutableListOf<String>()

        try {
            query.split(" ").forEach { q ->
                when {
                    q.startsWith("difficulty:") -> {
                        val range = q.removePrefix("difficulty:").split("-")
                        diffS = range[0].toInt()
                        diffE = range[1].toInt()
                    }

                    q.startsWith("contestid:") -> {
                        var rangeStr = q.removePrefix("contestid:")
                        if (rangeStr.endsWith("&odd")) {
                            cidOnlyOdd = true
                            rangeStr = rangeStr.removeSuffix("&odd")
                        }
                        val range = rangeStr.split("-")
                        cidS = range[0].toInt()
                        cidE = range[1].toInt()
                    }

                    q.startsWith("!@") -> {
                        forbiddenHandles.add(q.removePrefix("!@"))
                    }

                    q.startsWith("contesttype:") -> {
                        ctypes = q.removePrefix("contesttype:").split("|")
                    }

                    q.startsWith("problemid:") -> {
                        pids = q.removePrefix("problemid:").split("|").toSet()
                    }
                }
            }
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid query")
        }

        val contestList =
            webClient.get().uri("https://codeforces.com/api/contest.list?gym=false").retrieve()
                .awaitBody<CodeforcesResponse<CodeforcesContest>>()
        val problemSet =
            webClient.get().uri("https://codeforces.com/api/problemset.problems").retrieve()
                .awaitBody<CodeforcesProblemSetResponse>()

        val contestNames = contestList.result.associate { it.id to it.name.lowercase() }
        val allProblems = problemSet.result.problems.shuffled()

        val forbiddenProblemIds = forbiddenHandles.flatMap { handle ->
            webClient.get().uri("https://codeforces.com/api/user.status?handle=$handle")
                .retrieve()
                .awaitBody<CodeforcesResponse<CodeforcesSubmission>>()
                .result.map { "${it.problem.contestId}${it.problem.index}" }
        }.toSet()

        val resultProblems = allProblems.filter { problem ->
            val rating = problem.rating ?: return@filter false
            val contestId = problem.contestId ?: return@filter false

            if (diffS != null && diffE != null && (rating < diffS || rating > diffE)) return@filter false
            if (cidS != null && cidE != null && (contestId < cidS || contestId > cidE)) return@filter false
            if (cidOnlyOdd && contestId % 2 == 0) return@filter false
            if (pids.isNotEmpty() && !pids.contains(problem.index.take(1))) return@filter false

            val name = contestNames[contestId] ?: ""
            val typeMatched = ctypes.any { type ->
                when (type) {
                    "div1" -> "div. 1" in name && "div. 1 + div. 2" !in name
                    "div2" -> "div. 2" in name && "div. 1 + div. 2" !in name
                    "div3" -> "div. 3" in name
                    "div4" -> "div. 4" in name
                    "edu" -> "educational" in name
                    "global" -> "global" in name || "div. 1 + div. 2" in name
                    "etc" -> listOf(
                        "div. 1",
                        "div. 2",
                        "div. 3",
                        "div. 4",
                        "educational",
                        "global"
                    ).none { it in name }

                    else -> false
                }
            }
            if (!typeMatched) return@filter false

            val problemId = "${contestId}${problem.index}"
            !forbiddenProblemIds.contains(problemId)
        }

        return resultProblems.take(num).map {
            ProblemInfo(id = "${it.contestId}${it.index}", difficulty = it.rating ?: 0)
        }
    }
}
