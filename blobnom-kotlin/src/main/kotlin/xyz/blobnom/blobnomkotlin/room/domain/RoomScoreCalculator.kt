package xyz.blobnom.blobnomkotlin.room.domain

import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@Component
class RoomScoreCalculator {
    fun calculateAndApplyScores(room: Room) {
        val missions = room.missions
        val numMission = missions.size
        val boardWidth = ((3 + sqrt((12 * numMission - 3).toDouble()).toInt()) / 6 - 1) * 2 + 1

        val board = Array(boardWidth) { IntArray(boardWidth) { -1 } }
        var ptr = 0
        for (i in 0 until boardWidth) {
            val s = max(0, i - boardWidth / 2)
            val loopCount = boardWidth - abs(i - boardWidth / 2)
            for (j in 0 until loopCount) {
                board[s + j][i] = ptr
                ptr++
            }
        }

        val graph = Array(numMission) { mutableListOf<Int>() }
        for (i in 0 until boardWidth) {
            for (j in 0 until boardWidth) {
                val u = board[i][j]
                if (u < 0) continue
                connect(graph, u, board, i, j + 1, boardWidth)
                connect(graph, u, board, i + 1, j, boardWidth)
                connect(graph, u, board, i + 1, j + 1, boardWidth)
            }
        }

        val solvedTeamIndex = IntArray(numMission) { -1 }
        val teamTotalSolved = mutableMapOf<Int, Int>()
        val teamIndivSolved = mutableMapOf<Long, Int>()
        val teamLastSolvedAt = mutableMapOf<Int, ZonedDateTime>()

        room.players.forEach {
            teamLastSolvedAt[it.teamIndex] = room.createdAt
            teamTotalSolved[it.teamIndex] = 0
        }

        // 인접 최대 영역 제외 전부 계산
        missions.forEach { mission ->
            val index = mission.indexInRoom
            if (mission.isSolved) {
                val teamIdx = mission.solvedTeamIndex!!
                val solver = mission.solvedRoomPlayer!!

                solvedTeamIndex[index] = teamIdx
                teamTotalSolved[teamIdx] = teamTotalSolved.getOrDefault(teamIdx, 0) + 1
                teamIndivSolved[solver.id!!] = teamIndivSolved.getOrDefault(solver.id!!, 0) + 1

                val currentLast = teamLastSolvedAt[teamIdx] ?: room.createdAt
                if (mission.solvedAt != null && mission.solvedAt!!.isAfter(currentLast)) {
                    teamLastSolvedAt[teamIdx] = mission.solvedAt!!
                }
            }
        }

        // 인접 최대 영역 계산
        val teamMaxAdjacent = mutableMapOf<Int, Int>()
        val visited = BooleanArray(numMission) { false }

        for (i in 0 until numMission) {
            val currentTeamIdx = solvedTeamIndex[i]
            if (currentTeamIdx < 0 || visited[i]) continue

            val q: Queue<Int> = LinkedList()
            q.add(i)
            visited[i] = true
            var adjacentCount = 0

            while (q.isNotEmpty()) {
                val u = q.poll()
                adjacentCount++

                for (v in graph[u]) {
                    if (solvedTeamIndex[v] == currentTeamIdx && !visited[v]) {
                        visited[v] = true
                        q.add(v)
                    }
                }
            }
            val currentMax = teamMaxAdjacent.getOrDefault(currentTeamIdx, 0)
            teamMaxAdjacent[currentTeamIdx] = max(currentMax, adjacentCount)
        }
        room.players.forEach { player ->
            player.adjacentSolvedCount = teamMaxAdjacent.getOrDefault(player.teamIndex, 0)
            player.totalSolvedCount = teamTotalSolved.getOrDefault(player.teamIndex, 0)
            player.lastSolvedAt = teamLastSolvedAt[player.teamIndex] ?: room.createdAt
            player.indivSolvedCount = teamIndivSolved.getOrDefault(player.id!!, 0)
        }
    }

    private fun connect(graph: Array<MutableList<Int>>, u: Int, board: Array<IntArray>, r: Int, c: Int, width: Int) {
        if (r < width && c < width) {
            val v = board[r][c]
            if (v >= 0) {
                graph[u].add(v)
                graph[v].add(u)
            }
        }
    }
}