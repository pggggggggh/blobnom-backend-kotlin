import logging

from src.chat_service import send_system_chat
from src.pb.room_events_pb2 import RoomEvent
from src.socket_gateway import SocketGateway

logger = logging.getLogger(__name__)


async def handle_room_event(gateway: SocketGateway, event: RoomEvent):
    event_type = event.WhichOneof("event")
    match event_type:
        case "problem_solved":
            await handle_problem_solved(
                gateway,
                event.room_id,
                event.problem_solved.problem_id,
                event.problem_solved.username,
            )
        case "room_started":
            await handle_room_started(gateway, event.room_id)
        case "room_ready_failed":
            await handle_room_failed(gateway, event.room_id, event.room_ready_failed.message)
        case _:
            logger.info("알 수 없는 이벤트 타입")


async def handle_problem_solved(
        gateway: SocketGateway,
        room_id: int,
        problem_id: str,
        username: str,
):
    await gateway.emit_problem_solved(room_id, problem_id, username)
    await send_system_chat(gateway, room_id, f"{username}이 {problem_id}를 해결하였습니다!")


async def handle_room_started(gateway: SocketGateway, room_id: int):
    await send_system_chat(gateway, room_id, "게임이 시작되었습니다! 🔥")
    await gateway.emit_refresh(room_id)


async def handle_room_failed(gateway: SocketGateway, room_id: int, message: str):
    await send_system_chat(gateway, room_id, f"방 생성이 실패하였습니다, 1분 뒤 재시도합니다: {message}")
    await gateway.emit_refresh(room_id)
