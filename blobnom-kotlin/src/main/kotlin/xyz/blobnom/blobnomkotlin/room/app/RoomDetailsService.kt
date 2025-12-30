package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.member.app.toMemberSummary
import xyz.blobnom.blobnomkotlin.room.domain.Room
import xyz.blobnom.blobnomkotlin.room.domain.RoomMission
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import xyz.blobnom.blobnomkotlin.room.dto.RoomDetails
import xyz.blobnom.blobnomkotlin.room.dto.TeamInfo
import xyz.blobnom.blobnomkotlin.room.dto.TeamMemberInfo

@Service
@Transactional(readOnly = true)
class RoomDetailsService(
    private val roomRepository: RoomRepository,
) {
    // TODO: Resolve N+1
    fun getRoomDetails(roomId: Long, memberId: Long?): RoomDetails {
        val room: Room = roomRepository.findByIdOrNull(roomId) ?: throw RuntimeException()

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
        val missionInfos = room.missions.sortedBy { it.indexInRoom }.map(RoomMission::toInfo)

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