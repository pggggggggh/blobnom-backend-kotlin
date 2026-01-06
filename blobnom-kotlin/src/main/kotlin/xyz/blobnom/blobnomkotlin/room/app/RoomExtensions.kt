package xyz.blobnom.blobnomkotlin.room.app

import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.room.domain.Room
import xyz.blobnom.blobnomkotlin.room.domain.RoomMission
import xyz.blobnom.blobnomkotlin.room.dto.RoomDetails
import xyz.blobnom.blobnomkotlin.room.dto.RoomMissionInfo
import xyz.blobnom.blobnomkotlin.room.dto.RoomSummary
import xyz.blobnom.blobnomkotlin.room.dto.TeamInfo

fun Room.toRoomDetails(
    owner: MemberSummary?,
    isUserInRoom: Boolean,
    isOwnerAMember: Boolean,
    teamInfos: List<TeamInfo>,
    missionInfos: List<RoomMissionInfo>,
    yourUnsolvableMissionIds: List<Long>
) = RoomDetails(
    startsAt = startsAt,
    endsAt = endsAt,
    id = id!!,
    name = name,
    platform = platform,
    query = query,
    owner = owner,
    isPrivate = isPrivate,
    isUserInRoom = isUserInRoom,
    isOwnerAMember = isOwnerAMember,
    numMissions = numMission,
    isStarted = isStarted,
    modeType = modeType,
    boardType = boardType,
    teamInfos = teamInfos,
    missionInfos = missionInfos,
    yourUnsolvableMissionIds = yourUnsolvableMissionIds,
    isContestRoom = false,
    practiceId = null
)

fun Room.toRoomSummary(owner: MemberSummary?) = RoomSummary(
    id = id!!,
    name = name,
    platform = platform,
    startsAt = startsAt,
    endsAt = endsAt,
    owner = owner,
    numPlayers = players.size,
    maxPlayers = maxPlayers,
    numMissions = numMission,
    numSolvedMissions = numSolvedMissions,
    winner = winner,
    isPrivate = isPrivate,
    isContestRoom = isContestRoom,
)

fun RoomMission.toInfo(withDifficulty: Boolean = true) = RoomMissionInfo(
    id = id!!,
    problemId = problemId,
    platform = platform,
    indexInRoom = indexInRoom,
    solvedAt = solvedAt,
    solvedTeamIndex = solvedTeamIndex,
    difficulty = if (withDifficulty) difficulty else null
)