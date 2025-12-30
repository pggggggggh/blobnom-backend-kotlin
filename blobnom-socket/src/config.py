import os

REDIS_URL = f"redis://{os.environ.get('REDIS_HOST', 'localhost')}:{os.environ.get('REDIS_PORT', '6379')}/{os.environ.get('REDIS_DBNO', '0')}"
