package com.j15.backend.presentation.controller.code

import com.j15.backend.application.usecase.code.CodePreviewUseCase
import com.j15.backend.domain.model.assignment.Language
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** コードプレビューコントローラー */
@RestController
@RequestMapping("/api/code")
class CodePreviewController(private val codePreviewUseCase: CodePreviewUseCase) {

    @PostMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    fun preview(
            @Valid @RequestBody request: CodePreviewRequest
    ): ResponseEntity<CodePreviewResponse> {
        val language =
                try {
                    Language.valueOf(request.language.uppercase())
                } catch (e: IllegalArgumentException) {
                    return ResponseEntity.badRequest()
                            .body(
                                    CodePreviewResponse(
                                            output = null,
                                            executionTime = null,
                                            status = "ERROR",
                                            errorMessage = "未対応の言語: ${request.language}"
                                    )
                            )
                }

        val result =
                codePreviewUseCase.preview(
                        code = request.code,
                        language = language,
                        input = request.input ?: "",
                        timeLimit = request.timeLimit
                )

        return ResponseEntity.ok(
                CodePreviewResponse(
                        output = result.output,
                        executionTime = result.executionTime,
                        status = result.status.name,
                        errorMessage = result.errorMessage
                )
        )
    }
}

/** プレビューリクエスト */
data class CodePreviewRequest(
        @field:NotBlank(message = "コードは必須です") val code: String,
        @field:NotBlank(message = "言語は必須です") val language: String,
        val input: String? = null,
        val timeLimit: Int? = null
)

/** プレビューレスポンス */
data class CodePreviewResponse(
        val output: String?,
        val executionTime: Int?,
        val status: String,
        val errorMessage: String? = null
)
