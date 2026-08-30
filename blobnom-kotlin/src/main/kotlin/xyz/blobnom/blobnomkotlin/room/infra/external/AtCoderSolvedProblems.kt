package xyz.blobnom.blobnomkotlin.room.infra.external

interface AtCoderSolvedProblems {
    suspend fun fetchAll(handle: String): Set<String>
}
