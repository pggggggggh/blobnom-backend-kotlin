package xyz.blobnom.blobnomkotlin.room.app.port

interface RoomEventPublisherPort {
    fun publishProblemSolved(roomId: Long, problemId: String, username: String)
    fun publishRoomStarted(roomId: Long)
    fun publishRoomReadyFailed(roomId: Long, message: String)
}
