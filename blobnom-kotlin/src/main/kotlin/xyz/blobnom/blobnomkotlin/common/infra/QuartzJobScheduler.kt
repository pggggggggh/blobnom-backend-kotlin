package xyz.blobnom.blobnomkotlin.common.infra


import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.Date
import kotlin.reflect.KClass

@Component
class QuartzJobScheduler(
    private val scheduler: Scheduler,
) {
    fun scheduleOneShot(
        at: ZonedDateTime,
        jobName: String,
        jobClass: KClass<out Job>,
        jobDataMap: JobDataMap
    ) {
        val jobKey = JobKey.jobKey(jobName, "jobs")

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }

        val jobDetail = JobBuilder.newJob(jobClass.java)
            .withIdentity(jobName, "jobs")
            .usingJobData(jobDataMap)
            .storeDurably()
            .requestRecovery()
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(jobName, "triggers")
            .startAt(Date.from(at.toInstant()))
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionFireNow()
            )
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }
}