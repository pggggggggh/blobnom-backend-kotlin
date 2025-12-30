package xyz.blobnom.blobnomkotlin.room.dto

import xyz.blobnom.blobnomkotlin.common.Platform
import java.time.ZonedDateTime

data class RoomMissionInfo(
    val id: Long,
    val problemId: String,
    val platform: Platform,
    val indexInRoom: Int,
    val solvedAt: ZonedDateTime?,
    val solvedTeamIndex: Int?,
    val difficulty: Int?
)
