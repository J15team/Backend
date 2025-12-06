package com.j15.backend.domain.model.progress

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import java.time.Instant

// ユーザー進捗状態（集約ルート）
// 特定題材におけるユーザーのセクション完了状態を管理し、進捗率を計算する
data class UserProgress(
        val userId: UserId,
        val subjectId: SubjectId,
        val clearedSections: List<UserClearedSection>,
        val totalSections: Int // 題材の最大セクション数
) {

    /**
     * 進捗率を計算（0~100の整数パーセンテージ）
     * 小数点以下は切り捨て
     * 全完了時は必ず100%を返す
     * 動的なセクション数に対応
     */
    fun calculateProgressPercentage(): Int {
        if (totalSections == 0) {
            return 0
        }

        val clearedCount = clearedSections.size

        // 全セクション完了時は必ず100を返す
        if (clearedCount >= totalSections) {
            return 100
        }

        // 未完了の場合は整数除算で切り捨て（0~99の範囲）
        return (clearedCount * 100) / totalSections
    }

    /** 完了済みセクション数 */
    fun getClearedCount(): Int = clearedSections.size

    /** 未完了セクション数 */
    fun getRemainingCount(): Int = totalSections - clearedSections.size

    /** 指定セクションが完了済みかチェック */
    fun isSectionCleared(sectionId: SectionId): Boolean {
        return clearedSections.any { it.sectionId == sectionId }
    }

    /** 完了済みセクションIDのリストを取得 */
    fun getClearedSectionIds(): List<SectionId> {
        return clearedSections.map { it.sectionId }
    }

    /** 次に完了すべきセクションIDを提案（0から順番に） */
    fun suggestNextSection(): SectionId? {
        val clearedIds = getClearedSectionIds().map { it.value }.toSet()

        for (id in Section.MIN_SECTION_ID..Section.MAX_SECTION_ID) {
            if (!clearedIds.contains(id)) {
                return SectionId(id)
            }
        }

        return null // 全て完了済み
    }

    /** 全セクション完了済みか判定 */
    fun isAllCleared(): Boolean {
        return clearedSections.size >= totalSections
    }

    /**
     * セクション完了を記録（不変条件を集約内で保証）
     * 重複チェックを行い、新しい完了記録を追加した集約状態を返す
     *
     * @param sectionId 完了するセクションID
     * @return 新しい完了記録が追加された集約状態と新規作成された完了記録のペア。
     *         セクションが既に完了している場合は失敗を含む Result を返す
     */
    fun markSectionAsCleared(sectionId: SectionId): Result<Pair<UserProgress, UserClearedSection>> {
        return runCatching {
            // 重複チェック（集約の不変条件）
            require(!isSectionCleared(sectionId)) {
                "セクション ${sectionId.value} は既に完了しています"
            }

            // 新しい完了記録を作成
            val newCleared = UserClearedSection(
                userClearedSectionId = null,
                userId = userId,
                subjectId = subjectId,
                sectionId = sectionId,
                completedAt = Instant.now()
            )

            // 新しい集約状態と新規作成された完了記録を返す（イミュータブル）
            val updatedProgress = copy(clearedSections = clearedSections + newCleared)
            updatedProgress to newCleared
        }
    }

    /**
     * セクション完了を取り消し
     * 指定されたセクションの完了記録を削除した集約状態を返す
     *
     * @param sectionId 取り消すセクションID
     * @return 完了記録が削除された集約状態。
     *         セクションが完了していない場合は失敗を含む Result を返す
     */
    fun unmarkSectionAsCleared(sectionId: SectionId): Result<UserProgress> {
        return runCatching {
            require(isSectionCleared(sectionId)) {
                "セクション ${sectionId.value} は完了していません"
            }

            val updated = clearedSections.filter { it.sectionId != sectionId }
            copy(clearedSections = updated)
        }
    }

    companion object {
        /**
         * ファクトリメソッド
         * ユーザー進捗状態を構築する
         *
         * @param userId ユーザーID
         * @param subjectId 題材ID
         * @param clearedSections 完了済みセクションのリスト
         * @param totalSections 題材の最大セクション数
         * @return 構築されたユーザー進捗状態
         * @throws IllegalArgumentException 最大セクション数が1未満の場合
         */
        fun create(
            userId: UserId,
            subjectId: SubjectId,
            clearedSections: List<UserClearedSection>,
            totalSections: Int
        ): UserProgress {
            require(totalSections > 0) { "最大セクション数は1以上である必要があります" }
            return UserProgress(userId, subjectId, clearedSections, totalSections)
        }
    }
}
