package xyz.blobnom.blobnomkotlin.room.dto

import com.fasterxml.jackson.annotation.JsonProperty
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.room.domain.enums.BoardType
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import java.time.ZonedDateTime

data class RoomDetails(
    val startsAt: ZonedDateTime?,
    val endsAt: ZonedDateTime?,
    val id: Long,
    val name: String,
    val platform: Platform,
    val query: String?,
    val owner: MemberSummary?,
    val isPrivate: Boolean,
    val isUserInRoom: Boolean,
    @JsonProperty("is_owner_a_member")
    val isOwnerAMember: Boolean,
    val numMissions: Int,
    val isStarted: Boolean,
    val modeType: ModeType,
    val boardType: BoardType,
    @JsonProperty("team_info")
    val teamInfos: List<TeamInfo>,
    @JsonProperty("mission_info")
    val missionInfos: List<RoomMissionInfo>,
    val yourUnsolvableMissionIds: List<Long>,
    val isContestRoom: Boolean,
    val practiceId: Long?
)

