package xyz.blobnom.blobnomkotlin.room.infra.scheduler

import org.quartz.JobDataMap
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.room.app.port.RoomJobCommand
import xyz.blobnom.blobnomkotlin.room.app.port.RoomJobSchedulerPort
import xyz.blobnom.blobnomkotlin.room.app.port.RoomEndCommand
import xyz.blobnom.blobnomkotlin.room.app.port.RoomReadyCommand
import xyz.blobnom.blobnomkotlin.room.app.port.RoomStartCommand
import xyz.blobnom.blobnomkotlin.common.infra.QuartzJobScheduler

@Component
class QuartzRoomRoomJobScheduler(
    private val jobScheduler: QuartzJobScheduler,
) : RoomJobSchedulerPort {
    override fun schedule(command: RoomJobCommand) {
        when (command) {
            is RoomReadyCommand -> {
                jobScheduler.scheduleOneShot(
                    command.at,
                    "room-ready-${command.roomId}",
                    jobClass = RoomReadyJob::class,
                    jobDataMap = JobDataMap().apply {
                        put("roomId", command.roomId)
                    }
                )
            }

            is RoomStartCommand -> {
                jobScheduler.scheduleOneShot(
                    command.at,
                    "room-start-${command.roomId}",
                    jobClass = RoomStartJob::class,
                    jobDataMap = JobDataMap().apply {
                        put("roomId", command.roomId)
                    }
                )
            }

            is RoomEndCommand -> {
                jobScheduler.scheduleOneShot(
                    command.at,
                    "room-end-${command.roomId}",
                    jobClass = RoomEndJob::class,
                    jobDataMap = JobDataMap().apply {
                        put("roomId", command.roomId)
                    }
                )
            }
        }
    }

}