package com.j15.backend.presentation.exception

import com.j15.backend.domain.exception.AssignmentNotFoundException
import com.j15.backend.domain.exception.NoAssignmentException
import com.j15.backend.domain.exception.SectionNotFoundException
import com.j15.backend.infrastructure.client.JudgeServiceUnavailableException
import com.j15.backend.infrastructure.service.GitHubOAuthException
import com.j15.backend.infrastructure.service.GoogleOAuthException
import com.j15.backend.presentation.dto.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

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
                        .body(
                                ErrorResponse(
                                        message = errors,
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
        }

        // JSONパースエラー（必須フィールド欠落など）
        @ExceptionHandler(HttpMessageNotReadableException::class)
        fun handleHttpMessageNotReadableException(
                ex: HttpMessageNotReadableException
        ): ResponseEntity<ErrorResponse> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ErrorResponse(
                                        message = "リクエストの形式が不正です",
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
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
        fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
                logger.warn("Access denied: {}", ex.message)
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(
                                ErrorResponse(
                                        message = "アクセスが拒否されました",
                                        status = HttpStatus.FORBIDDEN.value()
                                )
                        )
        }

        // 認証エラー（SecurityException）
        @ExceptionHandler(SecurityException::class)
        fun handleSecurityException(ex: SecurityException): ResponseEntity<ErrorResponse> {
                logger.warn("Authentication failed: {}", ex.message)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(
                                ErrorResponse(
                                        message = "認証に失敗しました",
                                        status = HttpStatus.UNAUTHORIZED.value()
                                )
                        )
        }

        // Google OAuth認証エラー
        @ExceptionHandler(GoogleOAuthException::class)
        fun handleGoogleOAuthException(ex: GoogleOAuthException): ResponseEntity<ErrorResponse> {
                logger.warn("Google OAuth error: {}", ex.message)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ErrorResponse(
                                        message = "Google認証に失敗しました",
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
        }

        // GitHub OAuth認証エラー
        @ExceptionHandler(GitHubOAuthException::class)
        fun handleGitHubOAuthException(ex: GitHubOAuthException): ResponseEntity<ErrorResponse> {
                logger.warn("GitHub OAuth error: {}", ex.message)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ErrorResponse(
                                        message = "GitHub認証に失敗しました",
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
        }

        // ファイルアップロードサイズ超過エラー
        @ExceptionHandler(MaxUploadSizeExceededException::class)
        fun handleMaxUploadSizeExceededException(
                ex: MaxUploadSizeExceededException
        ): ResponseEntity<ErrorResponse> {
                logger.warn("File upload size exceeded: {}", ex.message)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ErrorResponse(
                                        message = "ファイルサイズが上限を超えています（最大5MB）",
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
        }

        // 課題題材が見つからない
        @ExceptionHandler(AssignmentNotFoundException::class)
        fun handleAssignmentNotFoundException(
                ex: AssignmentNotFoundException
        ): ResponseEntity<ErrorResponse> {
                logger.warn("Assignment not found: {}", ex.message)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                ErrorResponse(
                                        message = ex.message ?: "課題題材が見つかりません",
                                        status = HttpStatus.NOT_FOUND.value()
                                )
                        )
        }

        // セクションが見つからない
        @ExceptionHandler(SectionNotFoundException::class)
        fun handleSectionNotFoundException(
                ex: SectionNotFoundException
        ): ResponseEntity<ErrorResponse> {
                logger.warn("Section not found: {}", ex.message)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                ErrorResponse(
                                        message = ex.message ?: "セクションが見つかりません",
                                        status = HttpStatus.NOT_FOUND.value()
                                )
                        )
        }

        // 課題なしセクションへの提出
        @ExceptionHandler(NoAssignmentException::class)
        fun handleNoAssignmentException(ex: NoAssignmentException): ResponseEntity<ErrorResponse> {
                logger.warn("No assignment in section: {}", ex.message)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ErrorResponse(
                                        message = ex.message ?: "このセクションには課題がありません",
                                        status = HttpStatus.BAD_REQUEST.value()
                                )
                        )
        }

        // Judge Service利用不可
        @ExceptionHandler(JudgeServiceUnavailableException::class)
        fun handleJudgeServiceUnavailableException(
                ex: JudgeServiceUnavailableException
        ): ResponseEntity<ErrorResponse> {
                logger.error("Judge Service unavailable: {}", ex.message)
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(
                                ErrorResponse(
                                        message = ex.message ?: "判定サービスが利用できません",
                                        status = HttpStatus.SERVICE_UNAVAILABLE.value()
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
