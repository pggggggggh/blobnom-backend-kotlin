package xyz.blobnom.blobnomkotlin.room.domain

import java.time.ZonedDateTime

data class TeamStat(
    val sortedPlayers: List<RoomPlayer>,
    val teamIndex: Int,
    val adjacentSolvedCount: Int,
    val totalSolvedCount: Int,
    val lastSolvedAt: ZonedDateTime?
)
