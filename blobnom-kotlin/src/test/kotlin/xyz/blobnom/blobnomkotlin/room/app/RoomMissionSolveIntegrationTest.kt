package xyz.blobnom.blobnomkotlin.room.app

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import xyz.blobnom.blobnomkotlin.AbstractIntegrationTest
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.domain.Room
import xyz.blobnom.blobnomkotlin.room.domain.RoomMission
import xyz.blobnom.blobnomkotlin.room.domain.enums.BoardType
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RoomMissionSolveIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var roomUpdateService: RoomUpdateService

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @AfterEach
    fun cleanUp() {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun onlyOnePlayerCanSolveTheSameMissionConcurrently() {
        val fixture = createFixture()
        val pool = Executors.newFixedThreadPool(fixture.memberIds.size)
        val ready = CountDownLatch(fixture.memberIds.size)
        val start = CountDownLatch(1)

        try {
            val futures = fixture.memberIds.map { memberId ->
                pool.submit<Throwable?> {
                    ready.countDown()
                    start.await()

                    try {
                        roomUpdateService.confirmSolve(
                            roomId = fixture.roomId,
                            missionId = fixture.missionId,
                            memberId = memberId,
                        )
                        null
                    } catch (throwable: Throwable) {
                        throwable
                    }
                }
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Both solve requests must be ready.")
            start.countDown()

            val results = futures.map { it.get(10, TimeUnit.SECONDS) }
            val successCount = results.count { it == null }
            val failure = results.singleOrNull { it != null }

            assertEquals(1, successCount, "Exactly one solve request must succeed.")
            assertEquals(CustomException::class, failure?.let { it::class }, "Exactly one solve request must fail.")
            assertEquals(ErrorCode.ALREADY_SOLVED, (failure as CustomException).errorCode)
        } finally {
            start.countDown()
            pool.shutdownNow()
        }

        val roomWithMissions = roomRepository.findWithMissions(fixture.roomId)!!
        val solvedMission = roomWithMissions.missions.single { it.id == fixture.missionId }
        val roomWithPlayers = roomRepository.findWithPlayers(fixture.roomId)!!

        assertNotNull(solvedMission.solvedAt, "The mission must have a solved timestamp.")
        assertNotNull(solvedMission.solvedRoomPlayer, "The mission must have exactly one solver.")
        assertEquals(1, roomWithMissions.numSolvedMissions, "The room solve count must increase once.")
        assertEquals(
            1,
            roomWithPlayers.players.sumOf { it.indivSolvedCount },
            "The individual solve count must increase once.",
        )
    }

    private fun createFixture(): MissionSolveFixture {
        val members = memberRepository.saveAllAndFlush(createMembers())
        val now = ZonedDateTime.now()
        val room = Room(
            name = "mission-concurrency-room",
            query = "#dp",
            numMission = 1,
            startsAt = now.minusMinutes(1),
            endsAt = now.plusHours(1),
            isStarted = true,
            maxPlayers = members.size,
            isPrivate = false,
            modeType = ModeType.LAND_GRAB_SOLO,
            boardType = BoardType.HEXAGON,
            platform = Platform.BOJ,
            owner = null,
        )

        members.forEach { member ->
            room.enter(
                platformUser = member.platformUsers.single(),
                teamIdx = null,
                now = now,
            )
        }

        room.missions.add(
            RoomMission(
                indexInRoom = 0,
                room = room,
                platform = Platform.BOJ,
                problemId = "1000",
                difficulty = 1,
            )
        )

        val savedRoom = roomRepository.saveAndFlush(room)
        return MissionSolveFixture(
            roomId = savedRoom.id!!,
            missionId = savedRoom.missions.single().id!!,
            memberIds = members.map { it.id!! },
        )
    }

    private fun createMembers(): List<Member> = (1..2).map { userNo ->
        Member.create(
            handle = "solver$userNo",
            email = "solver$userNo@blobnom.xyz",
            hashedPassword = "hashed",
        ).apply {
            linkPlatform(Platform.BOJ, "boj-solver$userNo")
        }
    }

    private data class MissionSolveFixture(
        val roomId: Long,
        val missionId: Long,
        val memberIds: List<Long>,
    )
}
