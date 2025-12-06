package com.j15.backend.application.usecase

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserProgress
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserClearedSectionRepository
import com.j15.backend.domain.service.ProgressCalculationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 進捗管理ユースケース（アプリケーション層）
@Service
@Transactional
class ProgressUseCase(
        private val userClearedSectionRepository: UserClearedSectionRepository,
        private val progressCalculationService: ProgressCalculationService
) {

    /** セクション完了を記録 フロントエンドから完了通知を受け取り、DB に永続化 */
    fun markSectionAsCleared(userId: UserId, sectionId: SectionId): Result<UserClearedSection> {
        return runCatching {
            // セクションIDの妥当性検証
            progressCalculationService.validateSectionId(sectionId).getOrThrow()

            // 既に完了済みかチェック
            val alreadyCleared =
                    userClearedSectionRepository.existsByUserIdAndSectionId(userId, sectionId)

            if (alreadyCleared) {
                throw IllegalStateException("セクション ${sectionId.value} は既に完了しています")
            }

            // 完了記録を保存
            val clearedSection = UserClearedSection.create(userId, sectionId)
            userClearedSectionRepository.save(clearedSection)
        }
    }

    /** ユーザーの進捗状態を取得 ログイン後にフロントエンドの進捗バーに表示するためのデータを返す */
    @Transactional(readOnly = true)
    fun getUserProgress(userId: UserId): UserProgress {
        val clearedSections = userClearedSectionRepository.findByUserId(userId)
        return progressCalculationService.buildUserProgress(userId, clearedSections)
    }

    /** 特定セクションの完了状態をチェック */
    @Transactional(readOnly = true)
    fun isSectionCleared(userId: UserId, sectionId: SectionId): Boolean {
        return userClearedSectionRepository.existsByUserIdAndSectionId(userId, sectionId)
    }

    /** セクション完了を取り消し（デバッグ・管理用） */
    fun unmarkSectionAsCleared(userId: UserId, sectionId: SectionId): Result<Unit> {
        return runCatching {
            userClearedSectionRepository.deleteByUserIdAndSectionId(userId, sectionId)
        }
    }
}
