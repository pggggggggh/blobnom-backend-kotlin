import logging
from socketio import AsyncServer

from src.chat_service import handle_join_room, handle_user_message
from src.schemas import MessageData
from src.socket_gateway import SocketGateway

logger = logging.getLogger(__name__)


def register_socket_events(sio: AsyncServer):
    gateway = SocketGateway(sio)

    @sio.event
    async def connect(sid, environ, auth):
        logger.info(f"Client Connected: {sid}")

    @sio.event
    async def disconnect(sid):
        logger.info(f"Client Disconnected: {sid}")

    @sio.event
    async def join_room(sid, data):
        room_id = data["roomId"]
        await handle_join_room(gateway, sid, room_id)

    @sio.event
    async def room_send_message(sid, data):
        data = MessageData(**data)
        logger.info(data.payload)
        await handle_user_message(gateway, data)
