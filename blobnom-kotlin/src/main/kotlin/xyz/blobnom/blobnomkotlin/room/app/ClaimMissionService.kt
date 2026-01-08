package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.app.port.SolvedProblemsFetcherPort
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime

@Service
class ClaimMissionService(
    private val roomRepository: RoomRepository,
    private val solvedProblemsFetcherPort: SolvedProblemsFetcherPort,
    private val roomUpdateService: RoomUpdateService
) {
    @Transactional
    suspend fun claimMission(roomId: Long, missionId: Long, memberId: Long): Boolean {
        // TODO: Resolve N+1
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw RuntimeException("Room not found")
        // TODO: modify using isEnded
        if (room.endsAt.isBefore(ZonedDateTime.now())) throw CustomException(ErrorCode.ROOM_ENDED)
        val player = room.players.find { it.platformUser.member.id == memberId }
            ?: throw RuntimeException("Player not found")
        val mission = room.missions.find { it.id == missionId }
            ?: throw RuntimeException("Mission not found")
        if (mission.isSolved) throw CustomException(ErrorCode.ALREADY_SOLVED)

        val solvedIds = solvedProblemsFetcherPort.fetchSolvedProblemIds(
            handle = player.platformUser.handle,
            platform = player.platformUser.platform,
            targetProblemIds = listOf(mission.problemId),
        )
        if (solvedIds.contains(mission.problemId)) {
            roomUpdateService.confirmSolve(roomId, missionId, memberId)
            return true
        }
        throw CustomException(ErrorCode.MISSION_VERIFICATION_FAILED)
    }
}