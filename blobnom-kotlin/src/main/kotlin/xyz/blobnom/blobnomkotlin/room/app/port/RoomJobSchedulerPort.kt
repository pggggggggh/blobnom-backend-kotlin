package xyz.blobnom.blobnomkotlin.room.app.port

interface RoomJobSchedulerPort {
    fun schedule(command: RoomJobCommand)
}