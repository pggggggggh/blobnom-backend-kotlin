package xyz.blobnom.blobnomkotlin.analytics.dto

data class MemberSolvedCount(
    val memberId: Long,
    val numSolvedMissions: Long,
)