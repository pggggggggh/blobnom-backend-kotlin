package xyz.blobnom.blobnomkotlin.room.infra.external

import xyz.blobnom.blobnomkotlin.common.Platform

interface SolvedProblemFetcher {
    val platform: Platform
    suspend fun fetchSolvedProblemIds(handle: String, targetProblemIds: List<String>): List<String>
}
