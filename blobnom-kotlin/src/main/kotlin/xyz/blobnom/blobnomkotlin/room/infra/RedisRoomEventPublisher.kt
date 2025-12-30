package xyz.blobnom.blobnomkotlin.room.infra

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import problemSolved
import roomEvent
import roomReadyFailed
import roomStarted
import xyz.blobnom.blobnomkotlin.room.app.port.RoomEventPublisherPort

@Component
class RedisRoomEventPublisher(
    private val protoRedisTemplate: RedisTemplate<String, ByteArray>
) : RoomEventPublisherPort {
    override fun publishProblemSolved(roomId: Long, problemId: String, username: String) {
        val event = roomEvent {
            this.roomId = roomId
            this.problemSolved = problemSolved {
                this.problemId = problemId
                this.username = username
            }
        }
        protoRedisTemplate.convertAndSend("server_commands", event.toByteArray())
    }

    override fun publishRoomStarted(roomId: Long) {
        val event = roomEvent {
            this.roomId = roomId
            this.roomStarted = roomStarted {}
        }
        protoRedisTemplate.convertAndSend("server_commands", event.toByteArray())
    }

    override fun publishRoomReadyFailed(roomId: Long, message: String) {
        val event = roomEvent {
            this.roomId = roomId
            this.roomReadyFailed = roomReadyFailed { this.message = message }
        }
        protoRedisTemplate.convertAndSend("server_commands", event.toByteArray())
    }
}