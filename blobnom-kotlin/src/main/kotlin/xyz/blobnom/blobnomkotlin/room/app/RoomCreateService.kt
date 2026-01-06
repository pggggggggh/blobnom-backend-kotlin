package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.auth.app.AuthService
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.app.port.RoomJobSchedulerPort
import xyz.blobnom.blobnomkotlin.room.app.port.ProblemFetcherPort
import xyz.blobnom.blobnomkotlin.room.app.port.RoomEventPublisherPort
import xyz.blobnom.blobnomkotlin.room.app.port.RoomEndCommand
import xyz.blobnom.blobnomkotlin.room.app.port.RoomReadyCommand
import xyz.blobnom.blobnomkotlin.room.app.port.RoomStartCommand
import xyz.blobnom.blobnomkotlin.room.domain.*
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import xyz.blobnom.blobnomkotlin.room.domain.enums.BoardType
import xyz.blobnom.blobnomkotlin.room.dto.RoomCreateRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomCreateResponse
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes

@Service
class RoomCreateService(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val roomJobSchedulerPort: RoomJobSchedulerPort,
    private val problemFetcherPort: ProblemFetcherPort,
    private val authService: AuthService,
    private val roomEventPublisherPort: RoomEventPublisherPort,
) {
    @Transactional
    suspend fun createRoom(request: RoomCreateRequest, memberId: Long): RoomCreateResponse {
        val now = ZonedDateTime.now()
        Room.validateIsCreatable(request.startsAt, now)

        val owner: Member =
            memberRepository.findByIdOrNull(memberId) ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)

        // 생성 시점에 체크
        val numMissions = Room.sizeToNumMissions(request.size)
        val problems = problemFetcherPort.fetch(request.platform, request.query, numMissions)
        if (problems.size < numMissions) throw CustomException(ErrorCode.TOO_LESS_MISSIONS)

        val room = Room(
            name = request.title,
            query = request.query,
            owner = owner,
            platform = request.platform,
            numMission = numMissions,
            entryPwd = request.entryPin?.let { authService.hashPassword(it) },
            editPwd = request.editPassword?.let { authService.hashPassword(it) },
            modeType = request.mode,
            boardType = BoardType.HEXAGON,
            maxPlayers = request.maxPlayers,
            isStarted = false,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            isPrivate = request.isPrivate,
            lastSolvedAt = request.startsAt,
            unfreezeOffsetMinutes = request.unfreezeOffsetMinutes,
            winner = "",
            isContestRoom = false
        )
        request.handles.forEach { (username, teamIdx) ->
            val member = memberRepository.findWithPlatformUsersByHandle(username)
                ?: throw CustomException(ErrorCode.MEMBER_NOT_FOUND)
            val platformUser = member.platformUsers.find { it.platform == request.platform }
                ?: throw CustomException(ErrorCode.UNLINKED_PLATFORM)

            room.enter(
                platformUser = platformUser,
                teamIdx = teamIdx,
                now = now,
                alreadySolvedProblemIds = emptyList(),
            )
        }
        val savedRoom = roomRepository.save(room)

        roomJobSchedulerPort.schedule(RoomReadyCommand(at = savedRoom.deadline, roomId = savedRoom.id!!))

        return RoomCreateResponse(success = true, roomId = savedRoom.id!!)
    }

    @Transactional
    suspend fun handleRoomReady(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId) // TODO: resolve N+1
            ?: throw RuntimeException("Unknown room")
        try {
            room.setQueryWithExcludedHandles()

            val problems = problemFetcherPort.fetch(room.platform, room.query, room.numMission)
            if (problems.size < room.numMission) {
                throw CustomException(ErrorCode.TOO_LESS_MISSIONS)
            }
            room.missions.clear()
            room.missions.addAll(problems.take(room.numMission).mapIndexed { index, problemInfo ->
                RoomMission(
                    room = room,
                    problemId = problemInfo.id,
                    difficulty = problemInfo.difficulty,
                    platform = room.platform,
                    indexInRoom = index
                )
            }.toMutableList())

            roomJobSchedulerPort.schedule(RoomStartCommand(at = room.startsAt, roomId = roomId))
            roomJobSchedulerPort.schedule(RoomEndCommand(at = room.endsAt, roomId = roomId))
        } catch (e: Exception) {
            roomEventPublisherPort.publishRoomReadyFailed(roomId, e.message.toString())
            room.postpone(1.minutes)

            roomJobSchedulerPort.schedule(RoomReadyCommand(at = room.deadline, roomId = roomId))
        }
    }

    @Transactional
    fun handleRoomStart(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw RuntimeException("Unknown room")
        room.startGame()
        roomRepository.save(room)
        roomEventPublisherPort.publishRoomStarted(room.id!!)
    }

    @Transactional
    fun handleRoomEnd(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw RuntimeException("Unknown room")
        room.endGame()
    }
}
