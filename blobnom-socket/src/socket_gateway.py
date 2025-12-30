from socketio import AsyncServer

from src.schemas import MessagePayload


class SocketGateway:
    def __init__(self, sio: AsyncServer):
        self._sio = sio

    async def enter_room(self, sid: str, room_id: int):
        await self._sio.enter_room(sid, f"room_{room_id}")

    async def emit_previous_messages(self, sid: str, messages: list[MessagePayload]):
        await self._sio.emit(
            "previous_messages",
            [message.model_dump() for message in messages],
            to=sid,
        )

    async def emit_room_message(self, room_id: int, payload: MessagePayload):
        await self._sio.emit(
            "room_new_message",
            payload.model_dump(),
            room=f"room_{room_id}",
        )

    async def emit_problem_solved(self, room_id: int, problem_id: str, username: str):
        await self._sio.emit(
            "problem_solved",
            {
                "problemId": problem_id,
                "username": username,
            },
            room=f"room_{room_id}",
        )

    async def emit_refresh(self, room_id: int):
        await self._sio.emit("room_started", room=f"room_{room_id}")
