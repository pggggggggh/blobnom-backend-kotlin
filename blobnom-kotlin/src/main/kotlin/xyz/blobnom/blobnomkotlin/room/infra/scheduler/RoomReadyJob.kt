package xyz.blobnom.blobnomkotlin.room.infra.scheduler

import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.room.app.RoomCreateService

@Component
class RoomReadyJob(
    private val roomCreateService: RoomCreateService,
) : QuartzJobBean() {
    @Transactional
    override fun executeInternal(context: JobExecutionContext) {
        val jobDataMap = context.jobDetail.jobDataMap
        val roomId = jobDataMap.getLong("roomId")
        runBlocking {
            try {
                roomCreateService.handleRoomReady(roomId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}