package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.member.app.toMemberSummary
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import xyz.blobnom.blobnomkotlin.room.dto.RoomDetails
import xyz.blobnom.blobnomkotlin.room.dto.TeamInfo
import xyz.blobnom.blobnomkotlin.room.dto.TeamMemberInfo
import java.time.ZonedDateTime

@Service
@Transactional(readOnly = true)
class RoomDetailsService(
    private val roomRepository: RoomRepository,
) {
    fun getRoomDetails(roomId: Long, memberId: Long?): RoomDetails {
        val room = roomRepository.findWithPlayers(roomId) ?: throw RuntimeException()
        roomRepository.findWithMissions(roomId) ?: throw RuntimeException()

        val player = memberId?.let { room.players.find { it.platformUser.member.id == memberId } }
        val isUserInRoom = player != null
        val yourUnsolvableMissionIds = player?.unsolvableMissionIds ?: emptyList()

        val teamStats = room.getTeamStats()
        val teamInfos = teamStats.map {
            TeamInfo(
                memberInfos = it.sortedPlayers.map {
                    TeamMemberInfo(
                        memberSummary = it.platformUser.member.toMemberSummary(),
                        indivSolvedCount = it.indivSolvedCount
                    )
                },
                teamIndex = it.teamIndex,
                adjacentSolvedCount = it.adjacentSolvedCount,
                totalSolvedCount = it.totalSolvedCount,
                lastSolvedAt = it.lastSolvedAt,
            )
        }
        val now = ZonedDateTime.now()
        val showDifficulty = room.showDifficulty(now)
        val missionInfos = room.missions.sortedBy { it.indexInRoom }.map { it.toInfo(showDifficulty) }

        return room.toRoomDetails(
            owner = room.owner?.toMemberSummary(),
            isOwnerAMember = room.owner != null,
            isUserInRoom = isUserInRoom,
            teamInfos = teamInfos,
            missionInfos = missionInfos,
            yourUnsolvableMissionIds = yourUnsolvableMissionIds
        )
    }
}