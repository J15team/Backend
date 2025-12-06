package com.j15.backend.domain.service

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserProgress
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId

// 進捗管理ドメインサービス
class ProgressCalculationService {

    /** ユーザー進捗状態を構築 */
    fun buildUserProgress(
            userId: UserId,
            clearedSections: List<UserClearedSection>,
            totalSections: Int
    ): UserProgress {
        return UserProgress(userId, clearedSections, totalSections)
    }

    /** セクション完了の重複チェック 同じユーザーが同じセクションを重複して完了できないようにする */
    fun validateNoDuplicate(existingProgress: UserProgress, sectionId: SectionId): Result<Unit> {
        return if (existingProgress.isSectionCleared(sectionId)) {
            Result.failure(IllegalStateException("セクション ${sectionId.value} は既に完了しています"))
        } else {
            Result.success(Unit)
        }
    }

    /** セクションIDの有効性チェック */
    fun validateSectionId(sectionId: SectionId): Result<Unit> {
        return runCatching {
            // SectionIdのinit blockで既にチェックされているが、明示的に検証
            require(sectionId.value in 0..100) { "無効なセクションID: ${sectionId.value}" }
        }
    }
}
