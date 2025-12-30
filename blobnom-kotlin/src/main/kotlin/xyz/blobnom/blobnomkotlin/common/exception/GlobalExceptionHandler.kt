package xyz.blobnom.blobnomkotlin.common.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val errorResponse = ErrorResponse(errorCode.code, errorCode.message)

        return ResponseEntity(errorResponse, errorCode.httpStatus)
    }
}