package xyz.blobnom.blobnomkotlin.room.app.port

import java.time.ZonedDateTime

sealed interface RoomJobCommand

data class RoomReadyCommand(val at: ZonedDateTime, val roomId: Long) : RoomJobCommand
data class RoomStartCommand(val at: ZonedDateTime, val roomId: Long) : RoomJobCommand
data class RoomEndCommand(val at: ZonedDateTime, val roomId: Long) : RoomJobCommand