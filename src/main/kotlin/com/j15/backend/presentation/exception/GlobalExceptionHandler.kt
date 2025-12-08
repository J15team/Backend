package com.j15.backend.presentation.exception

import com.j15.backend.presentation.dto.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// グローバル例外ハンドラー
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    // バリデーションエラー
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
            ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors =
                ex.bindingResult.allErrors.joinToString(", ") { error ->
                    val fieldName = (error as? FieldError)?.field ?: "field"
                    val errorMessage = error.defaultMessage ?: "invalid"
                    "$fieldName: $errorMessage"
                }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(message = errors, status = HttpStatus.BAD_REQUEST.value()))
    }

    // ビジネスロジックエラー（IllegalArgumentException）
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
            ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponse(
                                message = ex.message ?: "不正なリクエストです",
                                status = HttpStatus.BAD_REQUEST.value()
                        )
                )
    }

    // Spring Securityのアクセス拒否例外（403 Forbiddenを返す）
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
            ex: AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: {}", ex.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse(
                                message = "アクセスが拒否されました",
                                status = HttpStatus.FORBIDDEN.value()
                        )
                )
    }

    // その他の例外
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ErrorResponse(
                                message = "サーバーエラーが発生しました",
                                status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                )
    }
}
