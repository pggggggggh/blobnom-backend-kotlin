package xyz.blobnom.blobnomkotlin.analytics.dto

import xyz.blobnom.blobnomkotlin.member.dto.MemberSummary

data class LeaderboardEntry(
    val memberSummary: MemberSummary,
    val numSolvedMissions: Long,
)
