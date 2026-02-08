package xyz.blobnom.blobnomkotlin.room.infra.external

import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.room.dto.ProblemInfo

interface ProblemFetcher {
    val platform: Platform
    suspend fun fetch(query: String, num: Int): List<ProblemInfo>
}