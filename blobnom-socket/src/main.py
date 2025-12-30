import asyncio
import logging

import socketio
from fastapi import FastAPI
from contextlib import asynccontextmanager

from src.config import REDIS_URL
from src.redis_listener import redis_command_listener
from src.socket_handler import register_socket_events

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

mgr = socketio.AsyncRedisManager(REDIS_URL)
sio = socketio.AsyncServer(
    async_mode="asgi",
    cors_allowed_origins="*",
    client_manager=mgr
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    register_socket_events(sio)
    task = asyncio.create_task(redis_command_listener(sio))
    logger.info("Redis listener task started")

    try:
        yield
    finally:
        # shutdown
        task.cancel()
        try:
            await task
        except asyncio.CancelledError:
            pass
        logger.info("Redis listener task stopped")


fastapi_app = FastAPI(lifespan=lifespan)
app = socketio.ASGIApp(sio, other_asgi_app=fastapi_app)
