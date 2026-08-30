package xyz.blobnom.blobnomkotlin.room.dto

import io.swagger.v3.oas.annotations.media.Schema
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import java.time.ZonedDateTime

data class RoomCreateRequest(
    val title: String,
    @field:Schema(
        description = """Problem filter query. For ATCODER, separate filters with spaces to combine them with AND, and separate values with `|` to combine them with OR. Supported filters: `difficulty:{min}-{max}`, `contesttype:abc|arc|agc|ahc|etc`, `contestid:{contestId}|...`, and `problemid:{problemIndex}|...`. `problemid` uses the index portion such as `a`, `b`, or `ex`, not a full task ID. To select exact tasks, use `problemset:abc350_a,abc350_b`; `problemset:` must be the first and only filter. Previously solved problem exclusions are appended by the server except for `problemset:` queries.""",
        example = "difficulty:800-1200 contesttype:abc|arc problemid:d|e",
    )
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
