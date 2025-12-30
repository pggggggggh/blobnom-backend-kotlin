package xyz.blobnom.blobnomkotlin.room.dto

data class RoomListRequest(
    val page: Int,
    val search: String = "",
    val activeOnly: Boolean = false,
    val myRoomOnly: Boolean = false,
)