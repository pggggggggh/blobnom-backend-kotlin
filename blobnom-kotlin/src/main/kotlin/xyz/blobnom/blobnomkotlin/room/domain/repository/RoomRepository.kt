package xyz.blobnom.blobnomkotlin.room.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
        ORDER BY r.lastSolvedAt DESC
    """
    )
    fun searchRooms(
        search: String,
        activeOnly: Boolean,
        pageable: Pageable
    ): Page<Room>
}