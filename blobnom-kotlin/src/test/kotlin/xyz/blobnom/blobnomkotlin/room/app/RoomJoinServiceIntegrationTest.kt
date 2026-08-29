package xyz.blobnom.blobnomkotlin.room.app

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import xyz.blobnom.blobnomkotlin.AbstractIntegrationTest
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.domain.Room
import xyz.blobnom.blobnomkotlin.room.domain.enums.BoardType
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RoomJoinServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var roomJoinService: RoomJoinService

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @AfterEach
    fun cleanUp() {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun concurrentJoinsAssignUniqueTeamIndexesInSoloMode() {
        val roomId = roomRepository.saveAndFlush(createSoloRoom()).id!!
        val memberIds = memberRepository.saveAllAndFlush(createMembers()).map { it.id!! }
        val pool = Executors.newFixedThreadPool(memberIds.size)
        val ready = CountDownLatch(memberIds.size)
        val start = CountDownLatch(1)

        try {
            val futures = memberIds.map { memberId ->
                pool.submit<Throwable?> {
                    ready.countDown()
                    start.await()

                    try {
                        TransactionTemplate(transactionManager).executeWithoutResult {
                            runBlocking {
                                roomJoinService.joinRoom(
                                    roomId = roomId,
                                    memberId = memberId,
                                    password = null,
                                )
                            }
                        }
                        null
                    } catch (throwable: Throwable) {
                        throwable
                    }
                }
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS), "All join requests must be ready.")
            start.countDown()

            val failures = futures.mapNotNull { it.get(10, TimeUnit.SECONDS) }
            assertTrue(
                failures.isEmpty(),
                "All join requests must succeed: ${failures.joinToString { it.message ?: it::class.simpleName.orEmpty() }}",
            )
        } finally {
            start.countDown()
            pool.shutdownNow()
        }

        val players = roomRepository.findWithPlayers(roomId)!!.players
        val duplicatedTeamIndexes = players
            .groupingBy { it.teamIndex }
            .eachCount()
            .filterValues { it > 1 }

        assertEquals(memberIds.size, players.size, "All players must be persisted.")
        assertTrue(
            duplicatedTeamIndexes.isEmpty(),
            "Team indexes must be unique. Duplicates: $duplicatedTeamIndexes",
        )
    }

    private fun createSoloRoom(): Room {
        val startsAt = ZonedDateTime.now().plusHours(1)
        return Room(
            name = "concurrency-room",
            query = "#dp",
            numMission = 7,
            startsAt = startsAt,
            endsAt = startsAt.plusHours(2),
            maxPlayers = 200,
            isPrivate = false,
            modeType = ModeType.LAND_GRAB_SOLO,
            boardType = BoardType.HEXAGON,
            platform = Platform.BOJ,
            owner = null,
        )
    }

    private fun createMembers(): List<Member> = (1..24).map { userNo ->
        Member.create(
            handle = "member$userNo",
            email = "member$userNo@blobnom.xyz",
            hashedPassword = "hashed",
        ).apply {
            linkPlatform(Platform.BOJ, "boj$userNo")
        }
    }
}
