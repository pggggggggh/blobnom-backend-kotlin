import json
import logging
from datetime import datetime
from typing import List

import pytz
from redis import asyncio as redis

from src.config import REDIS_URL
from src.schemas import MessageData, MessagePayload
from src.socket_gateway import SocketGateway

logger = logging.getLogger(__name__)


async def handle_join_room(gateway: SocketGateway, sid: str, room_id: int):
    await gateway.enter_room(sid, room_id)
    history = await get_chat_history(room_id)
    history.append(
        MessagePayload(type="system", message="greeting_message", time=str(datetime.now(pytz.UTC)))
    )
    await gateway.emit_previous_messages(sid, history)


async def handle_user_message(gateway: SocketGateway, data: MessageData):
    room_id = data.roomId
    payload = MessagePayload(
        handle=data.payload.handle,
        type=data.payload.type,
        message=data.payload.message,
        time=str(data.payload.time),
        team_index=data.payload.team_index,
    )
    await gateway.emit_room_message(room_id, payload)
    await save_chat_message(room_id, payload)


async def send_system_chat(gateway: SocketGateway, room_id: int, message: str):
    payload = MessagePayload(
        type="system",
        message=message,
        time=str(datetime.now(pytz.UTC)),
    )
    await gateway.emit_room_message(room_id, payload)
    await save_chat_message(room_id, payload)


async def save_chat_message(room_id: int, message: MessagePayload):
    redis_conn = None
    try:
        redis_conn = redis.from_url(REDIS_URL)
        key = f"chat:history:{room_id}"
        value = message.model_dump_json()
        await redis_conn.rpush(key, value)
    except Exception as e:
        logger.error(f"Failed to save chat: {e}")
    finally:
        if redis_conn:
            await redis_conn.close()


async def get_chat_history(room_id: int):
    redis_conn = None
    messages: List[MessagePayload] = []
    try:
        redis_conn = redis.from_url(REDIS_URL)
        key = f"chat:history:{room_id}"

        raw_list = await redis_conn.lrange(key, 0, -1)
        for raw_msg in raw_list:
            msg_dict = json.loads(raw_msg)
            msg = MessagePayload.model_validate(msg_dict)
            messages.append(msg)
    except Exception as e:
        logger.error(f"Failed to load history: {e}")
    finally:
        if redis_conn:
            await redis_conn.close()

    return messages
