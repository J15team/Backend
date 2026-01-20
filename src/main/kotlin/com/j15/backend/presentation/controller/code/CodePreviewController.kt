package com.j15.backend.presentation.controller.code

import com.j15.backend.application.usecase.code.CodePreviewUseCase
import com.j15.backend.domain.model.assignment.Language
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/code")
class CodePreviewController(private val codePreviewUseCase: CodePreviewUseCase) {

    @PostMapping("/preview")
    fun preview(@RequestBody request: CodePreviewRequest): ResponseEntity<CodePreviewResponse> {
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
                                            errorMessage =
                                                    "Unsupported language: ${request.language}"
                                    )
                            )
                }

        val result =
                codePreviewUseCase.preview(
                        code = request.code,
                        language = language,
                        input = request.input ?: "",
                        timeLimit = request.timeLimit ?: 2000
                )

        return ResponseEntity.ok(
                CodePreviewResponse(
                        output = result.output,
                        executionTime = result.executionTime,
                        status = result.status,
                        errorMessage = result.errorMessage
                )
        )
    }
}

data class CodePreviewRequest(
        val code: String,
        val language: String,
        val input: String? = null,
        val timeLimit: Int? = null
)

data class CodePreviewResponse(
        val output: String?,
        val executionTime: Int?,
        val status: String,
        val errorMessage: String?
)
