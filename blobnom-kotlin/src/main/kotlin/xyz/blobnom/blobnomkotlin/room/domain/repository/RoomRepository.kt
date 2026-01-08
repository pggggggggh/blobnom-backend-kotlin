package xyz.blobnom.blobnomkotlin.room.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import xyz.blobnom.blobnomkotlin.room.domain.Room

@Repository
interface RoomRepository : JpaRepository<Room, Long> {
    @Query(
        """
        SELECT r FROM Room r
        LEFT JOIN FETCH r.owner
        WHERE (r.name LIKE %:search%)
        AND (:activeOnly = false OR (r.isStarted = true and r.entryPwd IS NULL ))
        AND (r.modeType = "LAND_GRAB_SOLO" OR r.modeType = "LAND_GRAB_TEAM")
        ORDER BY r.lastSolvedAt DESC
    """
    )
    fun searchRooms(
        search: String,
        activeOnly: Boolean,
        pageable: Pageable
    ): Page<Room>

    @Query(
        """
        select distinct r from Room r
        left join fetch r.owner o
        left join fetch r.players p
        left join fetch p.platformUser pu
        left join fetch pu.member m
        where r.id = :roomId 
    """
    )
    fun findWithPlayers(@Param("roomId") roomId: Long): Room?

    @Query(
        """
        select distinct r from Room r
        left join fetch r.missions ms
        where r.id = :roomId
    """
    )
    fun findWithMissions(@Param("roomId") roomId: Long): Room?
}