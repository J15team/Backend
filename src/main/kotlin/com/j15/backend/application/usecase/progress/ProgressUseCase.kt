package com.j15.backend.application.usecase.progress

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserProgress
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.domain.repository.UserClearedSectionRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProgressUseCase(
        private val clearedSectionRepository: UserClearedSectionRepository,
        private val sectionRepository: SectionRepository,
        private val subjectRepository: SubjectRepository
) {

        fun markSectionAsCleared(
                userId: UserId,
                subjectId: SubjectId,
                sectionId: SectionId
        ): UserClearedSection {
                subjectRepository.findById(subjectId)
                        ?: throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")

                if (isSectionCleared(userId, subjectId, sectionId)) {
                        throw IllegalStateException("セクション ${sectionId.value} は既に完了しています")
                }
                val cleared =
                        UserClearedSection(
                                userClearedSectionId = null,
                                userId = userId,
                                subjectId = subjectId,
                                sectionId = sectionId,
                                completedAt = Instant.now()
                        )

                return clearedSectionRepository.save(cleared)
        }

        /** セクション完了を取り消し デバッグや管理機能で使用 */
        fun unmarkSectionAsCleared(userId: UserId, subjectId: SubjectId, sectionId: SectionId) {
                clearedSectionRepository.deleteByUserIdAndSubjectIdAndSectionId(
                        userId,
                        subjectId,
                        sectionId
                )
        }

        /** ユーザーの進捗状態を取得 進捗バーの表示やダッシュボードで使用 */
        @Transactional(readOnly = true)
        fun getUserProgress(userId: UserId, subjectId: SubjectId): UserProgress {
                // 題材の存在確認
                subjectRepository.findById(subjectId)
                        ?: throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")

                val clearedSections =
                        clearedSectionRepository.findByUserIdAndSubjectId(userId, subjectId)
                val totalSections = sectionRepository.countBySubjectId(subjectId)

                return UserProgress.create(userId, subjectId, clearedSections, totalSections)
        }

        /** セクション完了状態をチェック */
        @Transactional(readOnly = true)
        fun isSectionCleared(userId: UserId, subjectId: SubjectId, sectionId: SectionId): Boolean {
                return clearedSectionRepository.existsByUserIdAndSubjectIdAndSectionId(
                        userId,
                        subjectId,
                        sectionId
                )
        }
}
