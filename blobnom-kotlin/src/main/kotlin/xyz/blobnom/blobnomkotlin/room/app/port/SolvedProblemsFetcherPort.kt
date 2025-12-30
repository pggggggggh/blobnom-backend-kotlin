package xyz.blobnom.blobnomkotlin.room.app.port

import xyz.blobnom.blobnomkotlin.common.Platform

interface SolvedProblemsFetcherPort {
    suspend fun fetchSolvedProblemIds(
        handle: String,
        platform: Platform,
        targetProblemIds: List<String>
    ): List<String>
}