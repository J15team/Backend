package com.j15.backend.application.usecase

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserProgress
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.domain.repository.UserClearedSectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 進捗管理ユースケース（アプリケーション層）
@Service
@Transactional
class ProgressUseCase(
        private val userClearedSectionRepository: UserClearedSectionRepository,
        private val sectionRepository: SectionRepository,
        private val subjectRepository: SubjectRepository
) {

    /** セクション完了を記録 フロントエンドから完了通知を受け取り、DB に永続化 */
    fun markSectionAsCleared(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ): Result<UserClearedSection> {
        return runCatching {
            // 1. 集約を取得
            val progress = getUserProgress(userId, subjectId)

            // 2. 集約に操作を委譲（不変条件は集約が保証）
            val (_, newCleared) = progress.markSectionAsCleared(sectionId).getOrThrow()

            // 3. 新しく追加された完了記録のみを永続化
            userClearedSectionRepository.save(newCleared)
        }
    }

    /** ユーザーの題材ごと進捗状態を取得 ログイン後にフロントエンドの進捗バーに表示するためのデータを返す */
    @Transactional(readOnly = true)
    fun getUserProgress(userId: UserId, subjectId: SubjectId): UserProgress {
        // 題材の存在確認
        val subject =
                subjectRepository.findById(subjectId)
                        ?: throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")

        val clearedSections =
                userClearedSectionRepository.findByUserIdAndSubjectId(userId, subjectId)
        val totalSections = sectionRepository.countBySubjectId(subjectId) // 実際にDBに登録されているセクション総数を使用

        // ファクトリメソッドを使用
        return UserProgress.create(
                userId,
                subjectId,
                clearedSections,
                totalSections
        )
    }

    /** 特定セクションの完了状態をチェック */
    @Transactional(readOnly = true)
    fun isSectionCleared(userId: UserId, subjectId: SubjectId, sectionId: SectionId): Boolean {
        return userClearedSectionRepository.existsByUserIdAndSubjectIdAndSectionId(
                userId,
                subjectId,
                sectionId
        )
    }

    /** セクション完了を取り消し（デバッグ・管理用） */
    fun unmarkSectionAsCleared(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ): Result<Unit> {
        return runCatching {
            // 1. 集約を取得
            val progress = getUserProgress(userId, subjectId)

            // 2. 集約に操作を委譲（不変条件は集約が保証）
            progress.unmarkSectionAsCleared(sectionId).getOrThrow()

            // 3. 完了記録を削除
            userClearedSectionRepository.deleteByUserIdAndSubjectIdAndSectionId(
                    userId,
                    subjectId,
                    sectionId
            )
        }
    }
}
