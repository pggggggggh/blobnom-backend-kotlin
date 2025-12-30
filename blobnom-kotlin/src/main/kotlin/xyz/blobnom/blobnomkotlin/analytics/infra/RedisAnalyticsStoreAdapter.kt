package xyz.blobnom.blobnomkotlin.analytics.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import xyz.blobnom.blobnomkotlin.analytics.app.AnalyticsStorePort
import xyz.blobnom.blobnomkotlin.analytics.dto.Leaderboards
import xyz.blobnom.blobnomkotlin.analytics.dto.SiteStats

@Component
class RedisAnalyticsStoreAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : AnalyticsStorePort {
    override fun getStats(): SiteStats? =
        redisTemplate.opsForValue().get("stats")?.let { objectMapper.readValue(it, SiteStats::class.java) }

    override fun putStats(stats: SiteStats) =
        redisTemplate.opsForValue().set("stats", objectMapper.writeValueAsString(stats))

    override fun getLeaderboards(): Leaderboards? =
        redisTemplate.opsForValue().get("leaderboards")?.let { objectMapper.readValue(it, Leaderboards::class.java) }

    override fun putLeaderboards(leaderboards: Leaderboards) =
        redisTemplate.opsForValue().set("leaderboards", objectMapper.writeValueAsString(leaderboards))
}