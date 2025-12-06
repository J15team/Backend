package com.j15.backend.domain.model.progress

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId

// ユーザー進捗状態（集約ルート）
// ユーザーのセクション完了状態を管理し、進捗率を計算する
data class UserProgress(val userId: UserId, val clearedSections: List<UserClearedSection>) {

    /** 進捗率を計算（0~100の整数パーセンテージ） 小数点以下は切り捨て 全完了時は必ず100%を返す */
    fun calculateProgressPercentage(): Int {
        val clearedCount = clearedSections.size
        val totalSections = Section.TOTAL_SECTIONS

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
    fun getRemainingCount(): Int = Section.TOTAL_SECTIONS - clearedSections.size

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
        return clearedSections.size >= Section.TOTAL_SECTIONS
    }
}
