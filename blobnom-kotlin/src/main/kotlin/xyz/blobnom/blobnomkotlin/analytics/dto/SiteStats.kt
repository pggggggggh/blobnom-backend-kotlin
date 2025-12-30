package xyz.blobnom.blobnomkotlin.analytics.dto

import java.time.ZonedDateTime

data class SiteStats(
    val numSolvedMissions: Int,
    val numMembers: Long,
    val updatedAt: ZonedDateTime,
)