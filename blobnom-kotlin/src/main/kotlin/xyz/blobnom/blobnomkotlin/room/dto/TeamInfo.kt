package xyz.blobnom.blobnomkotlin.room.dto

import com.fasterxml.jackson.annotation.JsonProperty
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import java.time.ZonedDateTime

data class TeamInfo(
    @JsonProperty("users")
    val memberInfos: List<TeamMemberInfo>,
    val teamIndex: Int,
    val adjacentSolvedCount: Int,
    val totalSolvedCount: Int,
    val lastSolvedAt: ZonedDateTime?
)

data class TeamMemberInfo(
    @JsonProperty("user")
    val memberSummary: MemberSummary,
    @JsonProperty("indiv_solved_cnt")
    val indivSolvedCount: Int,
)