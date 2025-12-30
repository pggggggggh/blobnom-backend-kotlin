package xyz.blobnom.blobnomkotlin.room.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.SqlTypes
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import xyz.blobnom.blobnomkotlin.common.Platform
import xyz.blobnom.blobnomkotlin.common.exception.CustomException
import xyz.blobnom.blobnomkotlin.common.exception.ErrorCode
import xyz.blobnom.blobnomkotlin.member.domain.Member
import xyz.blobnom.blobnomkotlin.member.domain.PlatformUser
import xyz.blobnom.blobnomkotlin.room.domain.enums.BoardType
import xyz.blobnom.blobnomkotlin.room.domain.enums.ModeType
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Entity
@Table(
    name = "rooms",
    indexes = [
        Index(name = "idx_room_name", columnList = "name")
    ]
)
@SQLRestriction("is_deleted = false")
class Room(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    var name: String,

    @Column
    var query: String,

    @Column(name = "num_mission")
    var numMission: Int,

    @Column(name = "edit_pwd")
    var editPwd: String? = null,

    @Column(name = "entry_pwd")
    var entryPwd: String? = null,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    @Column(name = "starts_at")
    var startsAt: ZonedDateTime,

    @Column(name = "ends_at")
    var endsAt: ZonedDateTime,

    @Column(name = "is_started", nullable = false)
    var isStarted: Boolean = false,

    @Column(name = "max_players")
    var maxPlayers: Int = 16,

    @Column(name = "is_private")
    var isPrivate: Boolean,

    @Column(name = "unfreeze_offset_minutes")
    var unfreezeOffsetMinutes: Int? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "mode_type", nullable = false)
    var modeType: ModeType = ModeType.LAND_GRAB_SOLO,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "board_type", nullable = false)
    var boardType: BoardType = BoardType.HEXAGON,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "platform", nullable = false)
    var platform: Platform,

    @Column(name = "winning_team_index")
    var winningTeamIndex: Int = 0,

    @Column(nullable = false)
    var winner: String = "",

    @Column(name = "num_solved_missions", nullable = false)
    var numSolvedMissions: Int = 0,

    @Column(name = "last_solved_at")
    var lastSolvedAt: ZonedDateTime? = null,

    @Column(name = "is_contest_room", nullable = false)
    var isContestRoom: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: Member?,

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var missions: MutableList<RoomMission> = mutableListOf(),

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var players: MutableList<RoomPlayer> = mutableListOf(),

//    @OneToOne(mappedBy = "room", fetch = FetchType.LAZY)
//    var practiceSession: PracticeMember? = null,
) : BaseTimeEntity() {
    val deadline: ZonedDateTime
        get() = startsAt.minus(REGISTER_DEADLINE.toJavaDuration())

    companion object {
        val REGISTER_DEADLINE: Duration = 30.seconds

        fun validateIsCreatable(startsAt: ZonedDateTime, now: ZonedDateTime) {
            if (now.isAfter(startsAt.minus(REGISTER_DEADLINE.toJavaDuration()))) throw CustomException(ErrorCode.TOO_FAST_STARTSAT)
        }

        fun sizeToNumMissions(size: Int): Int {
            return 3 * size * (size + 1) + 1
        }
    }

    fun getTeamStats(): List<TeamStat> = // 순위가 높은 것부터 반환
        players.groupBy { it.teamIndex }.map { (teamIndex, listPlayers) ->
            val anyUser = listPlayers.first() // 현재는 player에 팀의 정보가 저장되고 있음
            val sortedPlayers = listPlayers.sortedByDescending { it.indivSolvedCount }

            TeamStat(
                sortedPlayers = sortedPlayers,
                teamIndex = teamIndex,
                adjacentSolvedCount = anyUser.adjacentSolvedCount,
                totalSolvedCount = anyUser.totalSolvedCount,
                lastSolvedAt = anyUser.lastSolvedAt
            )
        }.sortedWith(
            compareByDescending<TeamStat> { it.adjacentSolvedCount }
                .thenByDescending { it.totalSolvedCount }
                .thenBy { it.lastSolvedAt }
        )

    fun setQueryWithExcludedHandles() {
        if (this.query.startsWith("problemset:")) return

        val excludeHandles = this.players.joinToString(separator = " ") { "!@${it.platformUser.handle}" }
        this.query = "${this.query} $excludeHandles"
    }


    private fun validateIsEnterable(platformUser: PlatformUser, now: ZonedDateTime): Boolean {
        if (this.players.size >= this.maxPlayers) throw CustomException(ErrorCode.ROOM_FULL)
        if (this.players.any { player -> player.platformUser.id == platformUser.id })
            throw CustomException(ErrorCode.ALREADY_PARTICIPATED)

        if (now.isAfter(deadline) && now.isBefore(this.startsAt)) throw CustomException(ErrorCode.JOIN_DEADLINE_EXCEEDED)
        return true
    }

    fun enter(
        platformUser: PlatformUser,
        teamIdx: Int?, // null일 시 임의 참가
        now: ZonedDateTime,
        alreadySolvedProblemIds: List<String> = emptyList(),
    ) {
        validateIsEnterable(platformUser, now)

        val unsolvableMissionIds =
            this.missions
                .filter { it.problemId in alreadySolvedProblemIds }
                .mapNotNull { it.id }
                .toMutableList()

        val takenTeamIndices = this.players.map { it.teamIndex }.toSet()
        val finalTeamIdx = teamIdx ?: (0..Int.MAX_VALUE).first { it !in takenTeamIndices }

        val newPlayer = RoomPlayer.create(this, platformUser, finalTeamIdx, unsolvableMissionIds)
        this.players.add(newPlayer)
    }

    fun startGame() {
        if (this.isStarted) {
            throw IllegalStateException("이미 시작된 게임입니다.")
        }
        this.isStarted = true
    }

    fun endGame() {
        TODO()
    }

    fun solveMission(mission: RoomMission, solver: RoomPlayer, solvedAt: ZonedDateTime) {
        if (mission.isSolved || solver.unsolvableMissionIds.contains(mission.id)) throw CustomException(ErrorCode.UNSOLVABLE_PROBLEM)
        mission.markSolved(solver, solvedAt)
    }

    fun postpone(duration: Duration) {
        startsAt = startsAt.plus(duration.toJavaDuration())
        endsAt = endsAt.plus(duration.toJavaDuration())
    }
}