package com.j15.backend.domain.model.assignment

/** テスト結果エンティティ（ドメイン層） */
data class TestResult(
        val id: TestResultId,
        val submissionId: SubmissionId,
        val testCaseIndex: Int,
        val verdict: Verdict,
        val executionTime: Int? = null, // ミリ秒
        val memoryUsed: Int? = null, // KB
        val visible: Boolean,
        val actualOutput: String? = null, // visible=trueの場合のみ
        val errorMessage: String? = null
) {
    init {
        require(testCaseIndex >= 0) { "テストケースインデックスは0以上である必要があります" }

        executionTime?.let { require(it >= 0) { "実行時間は0以上である必要があります" } }

        memoryUsed?.let { require(it >= 0) { "メモリ使用量は0以上である必要があります" } }
    }
}
