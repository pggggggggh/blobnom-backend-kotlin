package xyz.blobnom.blobnomkotlin.room.app

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository

@Service
class RoomDeleteService(
    private val roomRepository: RoomRepository,
    private val authService: AuthService,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    suspend fun deleteRoom(roomId: Long, memberId: Long?, password: String?) {
        val room = roomRepository.findByIdOrNull(roomId) // TODO: resolve N+1
            ?: throw CustomException(ErrorCode.ROOM_NOT_FOUND)

        if (room.owner == null) {
            if (!authService.matchPassword(password!!, room.editPwd!!))
                throw CustomException(ErrorCode.INVALID_PASSWORD)
        } else {
            val member =
                memberRepository.findByIdOrNull(memberId!!) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)
            if (member != room.owner) throw RuntimeException("방장만 삭제할 수 있습니다.")
        }
        if (room.numSolvedMissions >= 2) throw RuntimeException("두 문제 이상 풀렸으므로 삭제할 수 없습니다.")

        room.isDeleted = true
        roomRepository.save(room)
    }
}