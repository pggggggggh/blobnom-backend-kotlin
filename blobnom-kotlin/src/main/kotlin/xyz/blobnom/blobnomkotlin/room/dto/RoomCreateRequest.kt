package xyz.blobnom.blobnomkotlin.room.dto

import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import java.time.ZonedDateTime

data class RoomCreateRequest(
    val title: String,
    val query: String,
    val platform: Platform,
    val entryPin: String?,
    val editPassword: String?,
    val size: Int,
    val mode: ModeType,
    val maxPlayers: Int,
    val startsAt: ZonedDateTime,
    val endsAt: ZonedDateTime,
    val isPrivate: Boolean,
    val unfreezeOffsetMinutes: Int?,
    val handles: Map<String, Int>
)