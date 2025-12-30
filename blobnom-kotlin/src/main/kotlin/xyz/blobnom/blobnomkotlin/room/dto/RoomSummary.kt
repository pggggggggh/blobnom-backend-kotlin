package xyz.blobnom.blobnomkotlin.room.dto

import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary
import xyz.blobnom.blobnomkotlin.common.Platform
import java.time.ZonedDateTime

data class RoomSummary(
    val id: Long,
    val name: String,
    val platform: Platform,
    val startsAt: ZonedDateTime,
    val endsAt: ZonedDateTime,
    val owner: MemberSummary?,
    val numPlayers: Int,
    val maxPlayers: Int,
    val numMissions: Int,
    val numSolvedMissions: Int,
    val winner: String,
    val isPrivate: Boolean,
    val isContestRoom: Boolean,
)
