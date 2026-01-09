package xyz.blobnom.blobnomkotlin.room.presentation

import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.blobnom.blobnomkotlin.auth.infra.CustomUserDetails
import xyz.blobnom.blobnomkotlin.room.app.RoomCreateService
import xyz.blobnom.blobnomkotlin.room.app.RoomDetailsService
import xyz.blobnom.blobnomkotlin.room.app.RoomListService
import xyz.blobnom.blobnomkotlin.room.app.ClaimMissionService
import xyz.blobnom.blobnomkotlin.room.app.RoomDeleteService
import xyz.blobnom.blobnomkotlin.room.app.RoomJoinService
import xyz.blobnom.blobnomkotlin.room.app.RoomLeaveService
import xyz.blobnom.blobnomkotlin.room.dto.ClaimMissionRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomCreateRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomCreateResponse
import xyz.blobnom.blobnomkotlin.room.dto.RoomDeleteRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomDetails
import xyz.blobnom.blobnomkotlin.room.dto.RoomJoinRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomListRequest
import xyz.blobnom.blobnomkotlin.room.dto.RoomListResponse

@RestController
@RequestMapping("/rooms")
class RoomController(
    val roomListService: RoomListService,
    private val roomDetailsService: RoomDetailsService,
    private val roomCreateService: RoomCreateService,
    private val claimMissionService: ClaimMissionService,
    private val roomJoinService: RoomJoinService,
    private val roomDeleteService: RoomDeleteService,
    private val roomLeaveService: RoomLeaveService
) {
    @GetMapping("/list")
    fun getRoomList(
        @ParameterObject roomListRequest: RoomListRequest,
        @AuthenticationPrincipal principal: CustomUserDetails?
    ): ResponseEntity<RoomListResponse> {
        val response = roomListService.getRoomList(roomListRequest, principal?.memberId)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/details/{roomId}")
    fun getRoomDetails(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal principal: CustomUserDetails?
    ): ResponseEntity<RoomDetails> {
        val roomDetails = roomDetailsService.getRoomDetails(roomId, principal?.memberId)

        return ResponseEntity.ok(roomDetails)
    }

    @PostMapping("/create")
    suspend fun createRoom(
        @RequestBody request: RoomCreateRequest,
        @AuthenticationPrincipal principal: CustomUserDetails
    ): ResponseEntity<RoomCreateResponse> {
        val response = roomCreateService.createRoom(request, principal.memberId)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/solved")
    suspend fun claimMission(
        @RequestBody request: ClaimMissionRequest,
        @AuthenticationPrincipal principal: CustomUserDetails
    ): ResponseEntity<Boolean> {
        val verdict = claimMissionService.claimMission(request.roomId, request.missionId, principal.memberId)

        return ResponseEntity.ok(verdict)
    }

    @PostMapping("/join/{id}")
    suspend fun joinRoom(
        @PathVariable id: Long,
        @RequestBody request: RoomJoinRequest,
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<Unit> {
        roomJoinService.joinRoom(
            roomId = id,
            password = request.password,
            memberId = user.memberId,
        )

        return ResponseEntity.ok().build()
    }

    @PostMapping("/delete/{id}")
    suspend fun deleteRoom(
        @PathVariable id: Long,
        @RequestBody request: RoomDeleteRequest,
        @AuthenticationPrincipal user: CustomUserDetails?
    ): ResponseEntity<Unit> {
        roomDeleteService.deleteRoom(id, user?.memberId, request.password)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/leave/{id}")
    fun leaveRoom(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: CustomUserDetails
    ): ResponseEntity<Unit> {
        roomLeaveService.leaveRoom(id, user.memberId)

        return ResponseEntity.ok().build()
    }
}