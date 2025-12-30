from typing import Optional

from pydantic import BaseModel


class MessagePayload(BaseModel):
    handle: Optional[str] = None
    type: str
    message: str
    time: str
    team_index: Optional[int] = None


class MessageData(BaseModel):
    roomId: int
    payload: MessagePayload
