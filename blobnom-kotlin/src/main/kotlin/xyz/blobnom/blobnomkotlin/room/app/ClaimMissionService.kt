package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.app.port.SolvedProblemsFetcherPort
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime

@Service
class ClaimMissionService(
    private val roomRepository: RoomRepository,
    private val solvedProblemsFetcherPort: SolvedProblemsFetcherPort,
    private val roomUpdateService: RoomUpdateService,
    transactionManager: PlatformTransactionManager,
) {
    private val readOnlyTransactionTemplate = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    suspend fun claimMission(roomId: Long, missionId: Long, memberId: Long): Boolean {
        // separated transaction before and after the db query
        val (handle, platform, problemId) = checkNotNull(
            readOnlyTransactionTemplate.execute {
                val room = roomRepository.findByIdOrNull(roomId)
                    ?: throw RuntimeException("Room not found")
                if (room.endsAt.isBefore(ZonedDateTime.now())) throw CustomException(ErrorCode.ROOM_ENDED)

                val player = room.players.find { it.platformUser.member?.id == memberId }
                    ?: throw RuntimeException("Player not found")
                val mission = room.missions.find { it.id == missionId }
                    ?: throw RuntimeException("Mission not found")
                if (mission.isSolved) throw CustomException(ErrorCode.ALREADY_SOLVED)

                Triple(
                    player.platformUser.handle,
                    player.platformUser.platform,
                    mission.problemId,
                )
            }
        )

        val solvedIds = solvedProblemsFetcherPort.fetchSolvedProblemIds(
            handle = handle,
            platform = platform,
            targetProblemIds = listOf(problemId),
        )
        if (solvedIds.contains(problemId)) {
            roomUpdateService.confirmSolve(roomId, missionId, memberId)
            return true
        }
        throw CustomException(ErrorCode.MISSION_VERIFICATION_FAILED)
    }
}
