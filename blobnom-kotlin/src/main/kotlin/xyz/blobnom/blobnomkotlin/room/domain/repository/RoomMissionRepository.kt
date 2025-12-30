package xyz.blobnom.blobnomkotlin.room.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import xyz.blobnom.blobnomkotlin.room.domain.RoomMission

@Repository
interface RoomMissionRepository : JpaRepository<RoomMission, Long> {
    fun countBySolvedAtIsNotNull(): Int
}