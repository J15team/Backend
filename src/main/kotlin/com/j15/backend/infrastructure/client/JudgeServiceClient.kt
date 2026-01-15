package com.j15.backend.infrastructure.client

import com.j15.backend.domain.model.assignment.Language
import com.j15.backend.domain.model.assignment.TestCase
import com.j15.backend.domain.model.assignment.Verdict

/** Judge Serviceクライアントインターフェース */
interface JudgeServiceClient {
    /**
     * コードを判定
     * @param code 提出コード
     * @param language 言語
     * @param testCases テストケースリスト
     * @param timeLimit 制限時間（ミリ秒）
     * @param memoryLimit メモリ制限（MB）
     * @return 判定結果リスト
     */
    fun judge(
            code: String,
            language: Language,
            testCases: List<TestCase>,
            timeLimit: Int,
            memoryLimit: Int
    ): List<JudgeResultDto>
}

/** Judge Serviceからの判定結果DTO */
data class JudgeResultDto(
        val index: Int,
        val verdict: Verdict,
        val executionTime: Int? = null,
        val memoryUsed: Int? = null,
        val actualOutput: String? = null,
        val errorMessage: String? = null
)
