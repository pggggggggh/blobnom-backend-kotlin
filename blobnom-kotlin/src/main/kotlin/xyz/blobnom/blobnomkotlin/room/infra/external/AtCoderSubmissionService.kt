package xyz.blobnom.blobnomkotlin.room.infra.external

import org.springframework.stereotype.Component

@Component
class AtCoderSubmissionService(
    private val api: AtCoderProblemsApi,
) : AtCoderSolvedProblems {
    override suspend fun fetchAll(handle: String): Set<String> {
        require(ATCODER_HANDLE_REGEX.matches(handle)) { "Invalid AtCoder handle" }

        val solvedIds = linkedSetOf<String>()
        var fromSecond = 0L

        while (true) {
            val submissions = api.fetchSubmissions(handle, fromSecond)
            solvedIds += submissions.asSequence()
                .filter { it.result == ACCEPTED_RESULT }
                .map { it.problemId }

            val latestEpochSecond = submissions.maxOfOrNull { it.epochSecond }
            if (latestEpochSecond == null) break
            check(latestEpochSecond >= fromSecond) { "AtCoder submission cursor did not advance" }

            fromSecond = latestEpochSecond + 1
            if (submissions.size < SUBMISSION_PAGE_SIZE) break
        }

        return solvedIds
    }

    companion object {
        private val ATCODER_HANDLE_REGEX = Regex("^[0-9A-Za-z_]+$")
        private const val ACCEPTED_RESULT = "AC"
        private const val SUBMISSION_PAGE_SIZE = 500
    }
}
