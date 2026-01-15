package com.j15.backend.application.usecase.code

import com.j15.backend.domain.model.assignment.Language
import com.j15.backend.infrastructure.client.CodePreviewClient
import com.j15.backend.infrastructure.client.JudgeServiceUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** コードプレビューユースケース */
@Service
class CodePreviewUseCase(private val codePreviewClient: CodePreviewClient) {
    private val logger = LoggerFactory.getLogger(CodePreviewUseCase::class.java)

    /**
     * コードを実行してプレビュー
     * @param code ソースコード
     * @param language 言語
     * @param input 標準入力
     * @param timeLimit 実行時間制限（ミリ秒）
     * @return 実行結果
     */
    fun preview(
            code: String,
            language: Language,
            input: String,
            timeLimit: Int? = null
    ): CodePreviewResult {
        return try {
            codePreviewClient.preview(code, language, input, timeLimit)
        } catch (e: JudgeServiceUnavailableException) {
            logger.error("Judge Service unavailable for preview", e)
            CodePreviewResult(
                    output = null,
                    errorMessage = "実行サービスに接続できません",
                    executionTime = null,
                    status = PreviewStatus.ERROR
            )
        }
    }
}

/** プレビュー結果 */
data class CodePreviewResult(
        val output: String?,
        val errorMessage: String?,
        val executionTime: Int?,
        val status: PreviewStatus
)

/** プレビューステータス */
enum class PreviewStatus {
    SUCCESS,
    COMPILE_ERROR,
    RUNTIME_ERROR,
    TIMEOUT,
    ERROR
}
