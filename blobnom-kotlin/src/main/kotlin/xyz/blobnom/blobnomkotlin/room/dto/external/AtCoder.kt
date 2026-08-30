package xyz.blobnom.blobnomkotlin.room.dto.external

import com.fasterxml.jackson.annotation.JsonProperty

data class AtCoderProblem(
    val id: String,
    @JsonProperty("contest_id")
    val contestId: String,
    @JsonProperty("problem_index")
    val problemIndex: String,
    val name: String,
    val title: String,
)

data class AtCoderProblemModel(
    val difficulty: Double? = null,
)

data class AtCoderSubmission(
    val id: Long,
    @JsonProperty("epoch_second")
    val epochSecond: Long,
    @JsonProperty("problem_id")
    val problemId: String,
    @JsonProperty("contest_id")
    val contestId: String,
    @JsonProperty("user_id")
    val userId: String,
    val result: String,
)
