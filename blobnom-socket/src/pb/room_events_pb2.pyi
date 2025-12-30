from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class RoomEvent(_message.Message):
    __slots__ = ()
    ROOM_ID_FIELD_NUMBER: _ClassVar[int]
    PROBLEM_SOLVED_FIELD_NUMBER: _ClassVar[int]
    ROOM_STARTED_FIELD_NUMBER: _ClassVar[int]
    ROOM_READY_FAILED_FIELD_NUMBER: _ClassVar[int]
    room_id: int
    problem_solved: ProblemSolved
    room_started: RoomStarted
    room_ready_failed: RoomReadyFailed
    def __init__(self, room_id: _Optional[int] = ..., problem_solved: _Optional[_Union[ProblemSolved, _Mapping]] = ..., room_started: _Optional[_Union[RoomStarted, _Mapping]] = ..., room_ready_failed: _Optional[_Union[RoomReadyFailed, _Mapping]] = ...) -> None: ...

class ProblemSolved(_message.Message):
    __slots__ = ()
    PROBLEM_ID_FIELD_NUMBER: _ClassVar[int]
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    problem_id: str
    username: str
    def __init__(self, problem_id: _Optional[str] = ..., username: _Optional[str] = ...) -> None: ...

class RoomStarted(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class RoomReadyFailed(_message.Message):
    __slots__ = ()
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...
