package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.app.port.SolvedProblemsFetcherPort
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime

@Service
class RoomJoinService(
    private val roomRepository: RoomRepository,
    private val authService: AuthService,
    private val solvedProblemsFetcherPort: SolvedProblemsFetcherPort,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    suspend fun joinRoom(roomId: Long, memberId: Long, password: String?) {
        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw RuntimeException("Room not found")
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RuntimeException("Member not found")

        val now = ZonedDateTime.now()
        if (room.isPrivate) {
            if (password.isNullOrBlank()) {
                throw CustomException(ErrorCode.INVALID_PASSWORD)
            }
            if (!authService.matchPassword(password, room.entryPwd!!)) {
                throw CustomException(ErrorCode.INVALID_PASSWORD)
            }
        }

        val platformUser = member.platformUsers.find { it.platform == room.platform }
            ?: throw CustomException(ErrorCode.UNLINKED_PLATFORM)
        val solvedProblemIds = if (room.isStarted) {
            val targetProblemIds = room.missions
                .filter { !it.isSolved }
                .map { it.problemId }

            solvedProblemsFetcherPort.fetchSolvedProblemIds(platformUser.handle, room.platform, targetProblemIds)
        } else {
            emptyList()
        }

        room.enter(
            platformUser = platformUser,
            teamIdx = null,
            now = now,
            alreadySolvedProblemIds = solvedProblemIds
        )
        roomRepository.save(room)
    }
}