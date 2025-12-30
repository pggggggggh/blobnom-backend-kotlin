package xyz.blobnom.blobnomkotlin.analytics.app

import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.analytics.dto.LeaderboardEntry
import xyz.blobnom.blobnomkotlin.analytics.dto.Leaderboards
import xyz.blobnom.blobnomkotlin.member.domain.repository.MemberRepository
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomMissionRepository
import xyz.blobnom.blobnomkotlin.analytics.dto.SiteStats
import xyz.blobnom.blobnomkotlin.analytics.infra.MemberStatsQueryRepository
import xyz.blobnom.blobnomkotlin.member.app.toMemberSummary
import java.time.ZonedDateTime

@Service
class AnalyticsService(
    private val roomMissionRepository: RoomMissionRepository,
    private val memberRepository: MemberRepository,
    private val analyticsStorePort: AnalyticsStorePort,
    private val memberStatsQueryRepository: MemberStatsQueryRepository
) {
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 0)
    @Transactional
    fun computeStats() {
        val numSolvedMissions = roomMissionRepository.countBySolvedAtIsNotNull()
        val numMembers = memberRepository.count()

        val siteStats = SiteStats(numSolvedMissions, numMembers, ZonedDateTime.now())
        analyticsStorePort.putStats(siteStats)
    }

    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 0)
    @Transactional
    fun computeLeaderboards() {
        val memberSolvedCounts = memberStatsQueryRepository.findLeaderboard(10, 0)
        val leaderboardEntries = memberSolvedCounts.map {
            LeaderboardEntry(
                memberSummary = memberRepository.findByIdOrNull(it.memberId)!!.toMemberSummary(),
                numSolvedMissions = it.numSolvedMissions
            )
        }

        val leaderboards = Leaderboards(
            leaderboards = leaderboardEntries,
            updatedAt = ZonedDateTime.now(),
        )
        analyticsStorePort.putLeaderboards(leaderboards)
    }


    fun getStats(): SiteStats? = analyticsStorePort.getStats()

    fun getLeaderboards(): Leaderboards? = analyticsStorePort.getLeaderboards()
}