package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.room.app.port.RoomEventPublisherPort
import xyz.blobnom.blobnomkotlin.room.domain.RoomScoreCalculator
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime

@Service
class RoomUpdateService(
    private val roomRepository: RoomRepository,
    private val roomScoreCalculator: RoomScoreCalculator,
    private val roomEventPublisherPort: RoomEventPublisherPort
) {
    @Transactional
    fun confirmSolve(roomId: Long, missionId: Long, memberId: Long) {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw RuntimeException("Room not found")
        val player = room.players.find { it.platformUser.member?.id == memberId }
            ?: throw RuntimeException("Player not found")
        val mission = room.missions.find { it.id == missionId }
            ?: throw RuntimeException("Mission not found")
        room.solveMission(
            mission = mission,
            solvedAt = ZonedDateTime.now(),
            solver = player
        )
        roomScoreCalculator.calculateAndApplyScores(room)
        roomEventPublisherPort.publishProblemSolved(roomId, mission.problemId, player.platformUser.member!!.handle)
    }
}
