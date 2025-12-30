package xyz.blobnom.blobnomkotlin.analytics.dto

import java.time.ZonedDateTime

data class Leaderboards(
    val updatedAt: ZonedDateTime,
    val leaderboards: List<LeaderboardEntry>?,
)