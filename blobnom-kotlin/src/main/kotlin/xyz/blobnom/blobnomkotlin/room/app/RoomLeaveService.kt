package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository


@Service
class RoomLeaveService(
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun leaveRoom(roomId: Long, memberId: Long) {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw RuntimeException("Room not found")
        if (room.owner?.id == memberId) throw CustomException(ErrorCode.CANNOT_LEAVE)
        val player = room.players.find { it.platformUser.member.id == memberId }
            ?: throw RuntimeException("Member not in room")
        if (player.adjacentSolvedCount > 0) throw CustomException(ErrorCode.CANNOT_LEAVE)
        room.players.remove(player)
    }
}