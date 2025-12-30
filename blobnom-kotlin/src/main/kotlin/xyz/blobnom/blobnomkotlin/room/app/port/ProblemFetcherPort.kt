package xyz.blobnom.blobnomkotlin.room.app.port

import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo

interface ProblemFetcherPort {
    suspend fun fetch(platform: Platform, query: String, num: Int): List<ProblemInfo>
}