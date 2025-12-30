package xyz.blobnom.blobnomkotlin.room.dto.external

data class CodeforcesResponse<T>(val result: List<T>)
data class CodeforcesSubmission(val problem: CodeforcesProblem, val verdict: String?)
data class CodeforcesProblem(val contestId: Int?, val index: String, val name: String, val rating: Int?)
data class CodeforcesContest(val id: Int, val name: String)

data class CodeforcesProblemSetResponse(val result: CodeforcesProblemSetResponseInner)
data class CodeforcesProblemSetResponseInner(val problems: List<CodeforcesProblem>)