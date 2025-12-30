package xyz.blobnom.blobnomkotlin.analytics.infra

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import xyz.blobnom.blobnomkotlin.analytics.dto.MemberSolvedCount

@Repository
class MemberStatsQueryRepository(
    private val em: EntityManager
) {
    fun findLeaderboard(limit: Int, offset: Int): List<MemberSolvedCount> {
        return em.createQuery(
            """
            select new xyz.blobnom.blobnomkotlin.analytics.dto.MemberSolvedCount(
              m.id, count(distinct rm.id)
            )
            from RoomMission rm
              join rm.solvedRoomPlayer rp
              join rp.platformUser pu
              join pu.member m
            where rm.solvedAt is not null
            group by m.id, m.handle
            order by count(distinct rm.id) desc
            """.trimIndent(),
            MemberSolvedCount::class.java
        )
            .setFirstResult(offset)
            .setMaxResults(limit)
            .resultList
    }
}