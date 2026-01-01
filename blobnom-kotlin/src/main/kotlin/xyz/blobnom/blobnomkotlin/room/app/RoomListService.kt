package xyz.blobnom.blobnomkotlin.room.app

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xyz.blobnom.blobnomkotlin.member.app.toMemberSummary
import xyz.blobnom.blobnomkotlin.room.dto.RoomListRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomListResponse
import xyz.blobnom.blobnomkotlin.room.domain.repository.RoomRepository

@Service
@Transactional(readOnly = true)
class RoomListService(
    private val roomRepository: RoomRepository,
) {
    fun getRoomList(request: RoomListRequest, memberId: Long?): RoomListResponse {
        val pageable = PageRequest.of(request.page, 20)

        val roomPage = roomRepository.searchRooms(
            search = request.search,
            activeOnly = request.activeOnly,
            pageable = pageable
        )
        val roomSummaries = roomPage.content.map {
            it.toRoomSummary(owner = it.owner?.toMemberSummary())
        }

        return RoomListResponse(
            roomList = roomSummaries,
            upcomingContestList = emptyList(),
            totalPages = roomPage.totalPages
        )
    }
}