package xyz.blobnom.blobnomkotlin.analytics.app

import xyz.blobnom.blobnomkotlin.analytics.dto.Leaderboards
import xyz.blobnom.blobnomkotlin.analytics.dto.SiteStats

interface AnalyticsStorePort {
    fun getStats(): SiteStats?
    fun putStats(stats: SiteStats)

    fun getLeaderboards(): Leaderboards?
    fun putLeaderboards(leaderboards: Leaderboards)
}
