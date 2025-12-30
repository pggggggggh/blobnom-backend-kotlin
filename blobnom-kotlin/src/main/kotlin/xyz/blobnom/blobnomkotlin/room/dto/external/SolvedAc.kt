package xyz.blobnom.blobnomkotlin.room.dto.external

data class SolvedAcSearchResponse(
    val items: List<SolvedAcProblemInfo>
)

data class SolvedAcProblemInfo(
    val problemId: Int,
    val level: Int
)