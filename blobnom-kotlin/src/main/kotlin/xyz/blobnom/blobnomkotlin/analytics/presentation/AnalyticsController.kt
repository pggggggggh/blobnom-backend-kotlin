package xyz.blobnom.blobnomkotlin.analytics.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.blobnom.blobnomkotlin.analytics.app.AnalyticsService
import xyz.blobnom.blobnomkotlin.analytics.dto.Leaderboards
import xyz.blobnom.blobnomkotlin.analytics.dto.SiteStats

@RestController
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {
    @GetMapping("/stats")
    fun getStats(): SiteStats? = analyticsService.getStats()

    @GetMapping("/leaderboards")
    fun getLeaderboards(): Leaderboards? = analyticsService.getLeaderboards()
}