import asyncio
import logging
from socketio import AsyncServer

import redis.asyncio as redis

from src.room_events import handle_room_event
from src.config import REDIS_URL
from src.pb.room_events_pb2 import RoomEvent
from src.socket_gateway import SocketGateway

logger = logging.getLogger(__name__)


async def redis_command_listener(sio: AsyncServer):
    gateway = SocketGateway(sio)
    r = redis.from_url(REDIS_URL)
    pubsub = r.pubsub()
    await pubsub.subscribe("server_commands")
    logger.info("Redis Listener Started...")

    try:
        async for message in pubsub.listen():
            if message.get("type") != "message":
                continue
            try:
                data = message["data"]
                event = RoomEvent()
                event.ParseFromString(data)

                await handle_room_event(gateway, event)
            except Exception as e:
                logger.error(f"Error processing message: {e}")

    except asyncio.CancelledError:
        logger.info("Redis listener cancelled")
        raise
    finally:
        await pubsub.unsubscribe("server_commands")
        await pubsub.close()
        await r.close()
        logger.info("Redis listener cleaned up")
