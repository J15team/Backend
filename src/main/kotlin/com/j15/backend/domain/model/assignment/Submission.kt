package com.j15.backend.domain.model.assignment

import com.j15.backend.domain.model.user.UserId
import java.time.Instant

/** 提出エンティティ（ドメイン層） INSERT only（更新・削除禁止） */
data class Submission(
        val id: SubmissionId,
        val userId: UserId,
        val assignmentSubjectId: AssignmentSubjectId,
        val sectionId: AssignmentSectionId,
        val code: String,
        val language: Language,
        val submittedAt: Instant,
        val status: SubmissionStatus,
        val score: Int? = null, // 部分点（0-100）
        val totalTestCases: Int? = null,
        val passedTestCases: Int? = null
) {
    init {
        require(code.isNotBlank()) { "提出コードは空にできません" }

        // スコアのバリデーション
        score?.let { require(it in 0..100) { "スコアは0〜100の範囲である必要があります" } }

        // テストケース数のバリデーション
        if (totalTestCases != null && passedTestCases != null) {
            require(passedTestCases <= totalTestCases) { "通過テストケース数は全テストケース数以下である必要があります" }
        }
    }
}
