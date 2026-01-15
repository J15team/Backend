package com.j15.backend.infrastructure.client

import com.j15.backend.application.usecase.code.CodePreviewResult
import com.j15.backend.application.usecase.code.PreviewStatus
import com.j15.backend.domain.model.assignment.Language
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/** コードプレビュークライアント */
@Component
@EnableConfigurationProperties(JudgeServiceProperties::class)
class CodePreviewClient(
        private val properties: JudgeServiceProperties,
        webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(CodePreviewClient::class.java)
    private val webClient: WebClient = webClientBuilder.baseUrl(properties.baseUrl).build()

    /** コードを実行してプレビュー */
    fun preview(
            code: String,
            language: Language,
            input: String,
            timeLimit: Int? = null
    ): CodePreviewResult {
        val request =
                PreviewRequest(
                        code = code,
                        language = language.name.lowercase(),
                        input = input,
                        timeLimit = timeLimit
                )

        return try {
            val response =
                    webClient
                            .post()
                            .uri("/api/code/preview")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(PreviewResponse::class.java)
                            .block(Duration.ofMillis(properties.timeout))

            if (response == null) {
                return CodePreviewResult(
                        output = null,
                        errorMessage = "実行結果を取得できませんでした",
                        executionTime = null,
                        status = PreviewStatus.ERROR
                )
            }

            val status =
                    when (response.status) {
                        "SUCCESS" -> PreviewStatus.SUCCESS
                        "COMPILE_ERROR" -> PreviewStatus.COMPILE_ERROR
                        "RUNTIME_ERROR" -> PreviewStatus.RUNTIME_ERROR
                        "TIMEOUT" -> PreviewStatus.TIMEOUT
                        else -> PreviewStatus.ERROR
                    }

            CodePreviewResult(
                    output = response.output,
                    errorMessage = response.errorMessage,
                    executionTime = response.executionTime,
                    status = status
            )
        } catch (e: WebClientResponseException) {
            logger.error("Code preview error: ${e.statusCode} - ${e.responseBodyAsString}")
            throw JudgeServiceUnavailableException("プレビューサービスからエラーが返されました: ${e.statusCode}")
        } catch (e: Exception) {
            logger.error("Code preview connection error", e)
            throw JudgeServiceUnavailableException("プレビューサービスに接続できません")
        }
    }
}

/** プレビューリクエスト */
private data class PreviewRequest(
        val code: String,
        val language: String,
        val input: String,
        val timeLimit: Int?
)

/** プレビューレスポンス */
private data class PreviewResponse(
        val output: String?,
        val executionTime: Int?,
        val status: String,
        val errorMessage: String? = null
)
