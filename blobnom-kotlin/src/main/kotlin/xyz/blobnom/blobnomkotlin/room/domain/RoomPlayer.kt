package xyz.blobnom.blobnomkotlin.room.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import xyz.blobnom.blobnomkotlin.member.domain.PlatformUser
import java.time.ZonedDateTime

@Entity
@Table(name = "room_players")
class RoomPlayer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

//    @Column(name = "player_index", nullable = false)
//    var playerIndex: Int,

    @Column(name = "team_index")
    var teamIndex: Int,

    @Column(name = "adjacent_solved_count", nullable = false)
    var adjacentSolvedCount: Int = 0,

    @Column(name = "total_solved_count", nullable = false)
    var totalSolvedCount: Int = 0,

    @Column(name = "indiv_solved_count", nullable = false)
    var indivSolvedCount: Int = 0,

    @Column(name = "last_solved_at")
    var lastSolvedAt: ZonedDateTime? = null,

    @Column
    var rank: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var platformUser: PlatformUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    var room: Room,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "unsolvable_mission_ids", columnDefinition = "integer[]")
    var unsolvableMissionIds: List<Long> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "next_solvable_mission_ids", columnDefinition = "integer[]")
    var nextSolvableMissionIds: List<Long>? = mutableListOf()
) : BaseTimeEntity() {
    companion object {
        fun create(
            room: Room,
            platformUser: PlatformUser,
            teamIdx: Int,
            unsolvableMissionIds: List<Long>
        ): RoomPlayer {
            return RoomPlayer(
                room = room,
                platformUser = platformUser,
                teamIndex = teamIdx,
                lastSolvedAt = room.startsAt,
                adjacentSolvedCount = 0,
                totalSolvedCount = 0,
                indivSolvedCount = 0,
                unsolvableMissionIds = unsolvableMissionIds
            )
        }
    }
}