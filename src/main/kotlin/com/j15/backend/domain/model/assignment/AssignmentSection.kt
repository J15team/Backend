package com.j15.backend.domain.model.assignment

/** 課題セクションエンティティ（ドメイン層） 説明のみのセクションと課題ありセクションを混在可能 */
data class AssignmentSection(
        val assignmentSubjectId: AssignmentSubjectId,
        val sectionId: AssignmentSectionId,
        val title: String,
        val description: String? = null,
        val hasAssignment: Boolean = false,
        val testCases: String? = null, // JSON形式
        val timeLimit: Int? = null, // ミリ秒
        val memoryLimit: Int? = null // MB
) {
    init {
        require(title.isNotBlank()) { "課題セクションのタイトルは空にできません" }

        // hasAssignment と testCases の整合性チェック
        if (hasAssignment) {
            require(!testCases.isNullOrBlank()) { "課題ありの場合、テストケースは必須です" }
            require(timeLimit != null && timeLimit > 0) { "課題ありの場合、制限時間は正の値である必要があります" }
            require(memoryLimit != null && memoryLimit > 0) { "課題ありの場合、メモリ制限は正の値である必要があります" }
        } else {
            require(testCases == null) { "課題なしの場合、テストケースは設定できません" }
        }
    }

    companion object {
        const val DEFAULT_TIME_LIMIT = 2000 // 2秒
        const val DEFAULT_MEMORY_LIMIT = 256 // 256MB
    }
}
