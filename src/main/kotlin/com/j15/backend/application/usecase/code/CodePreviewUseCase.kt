package com.j15.backend.application.usecase.code

import com.j15.backend.domain.model.assignment.Language
import com.j15.backend.infrastructure.client.JudgeServiceClient
import com.j15.backend.infrastructure.client.JudgeServiceUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CodePreviewUseCase(private val judgeServiceClient: JudgeServiceClient) {
    private val logger = LoggerFactory.getLogger(CodePreviewUseCase::class.java)

    fun preview(
            code: String,
            language: Language,
            input: String,
            timeLimit: Int = 2000
    ): CodePreviewResult {
        return try {
            val result =
                    judgeServiceClient.run(
                            code = code,
                            language = language,
                            input = input,
                            timeLimit = timeLimit
                    )
            CodePreviewResult(
                    output = result.output,
                    executionTime = result.executionTime,
                    status = result.status,
                    errorMessage = result.errorMessage
            )
        } catch (e: JudgeServiceUnavailableException) {
            logger.error("Judge Service unavailable: ${e.message}")
            CodePreviewResult(
                    output = null,
                    executionTime = null,
                    status = "ERROR",
                    errorMessage = "Judge Service is unavailable"
            )
        } catch (e: Exception) {
            logger.error("Code preview failed: ${e.message}", e)
            CodePreviewResult(
                    output = null,
                    executionTime = null,
                    status = "ERROR",
                    errorMessage = e.message
            )
        }
    }
}

data class CodePreviewResult(
        val output: String?,
        val executionTime: Int?,
        val status: String,
        val errorMessage: String?
)
