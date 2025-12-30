package xyz.blobnom.blobnomkotlin.room.dto

data class RoomListResponse(
    val totalPages: Int,
    val roomList: List<RoomSummary>,
    val upcomingContestList: List<Any> // TODO
)
