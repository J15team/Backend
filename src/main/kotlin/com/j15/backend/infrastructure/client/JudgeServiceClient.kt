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

        /**
         * コードを実行（プレビュー用）
         * @param code 実行コード
         * @param language 言語
         * @param input 標準入力
         * @param timeLimit 制限時間（ミリ秒）
         * @return 実行結果
         */
        fun run(code: String, language: Language, input: String, timeLimit: Int): RunResultDto
}

/** コード実行結果DTO */
data class RunResultDto(
        val output: String?,
        val executionTime: Int?,
        val status: String,
        val errorMessage: String?
)

/** Judge Serviceからの判定結果DTO */
data class JudgeResultDto(
        val index: Int,
        val verdict: Verdict,
        val executionTime: Int? = null,
        val memoryUsed: Int? = null,
        val actualOutput: String? = null,
        val errorMessage: String? = null
)
