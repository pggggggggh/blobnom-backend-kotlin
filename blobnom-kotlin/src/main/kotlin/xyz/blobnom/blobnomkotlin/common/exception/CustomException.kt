package xyz.blobnom.blobnomkotlin.common.exception

class CustomException(
    val errorCode: ErrorCode,
    val exception: Exception? = null
) : RuntimeException(errorCode.message, exception)