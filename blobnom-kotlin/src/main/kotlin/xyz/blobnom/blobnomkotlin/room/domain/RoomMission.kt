package xyz.blobnom.blobnomkotlin.room.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import xyz.blobnom.blobnomkotlin.common.BaseTimeEntity
import xyz.blobnom.blobnomkotlin.common.Platform
import java.time.ZonedDateTime

@Entity
@Table(name = "room_missions")
class RoomMission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var indexInRoom: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    var room: Room,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "platform", nullable = false)
    var platform: Platform,

    @Column(nullable = false)
    var problemId: String,

    @Column
    var difficulty: Int? = 0,

    @Column
    var solvedTeamIndex: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solved_room_player_id")
    var solvedRoomPlayer: RoomPlayer? = null,

    @Column
    var solvedAt: ZonedDateTime? = null,

    ) : BaseTimeEntity() {
    val isSolved: Boolean
        get() = solvedAt != null && solvedRoomPlayer != null

    fun markSolved(solver: RoomPlayer, solvedAt: ZonedDateTime) {
        solvedTeamIndex = solver.teamIndex
        solvedRoomPlayer = solver
        this.solvedAt = solvedAt
    }
}