package xyz.blobnom.blobnomkotlin.room.infra.external

import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblem
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderProblemModel
import xyz.blobnom.blobnomkotlin.room.dto.external.AtCoderSubmission

interface AtCoderProblemsApi {
    suspend fun fetchProblems(): List<AtCoderProblem>

    suspend fun fetchProblemModels(): Map<String, AtCoderProblemModel>

    suspend fun fetchSubmissions(handle: String, fromSecond: Long): List<AtCoderSubmission>
}
