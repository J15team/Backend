package com.j15.backend.presentation.dto.response

import com.j15.backend.domain.model.progress.UserProgress
import java.time.Instant

// 進捗状態レスポンスDTO
data class UserProgressResponse(
        val userId: String,
        val progressPercentage: Double,
        val clearedCount: Int,
        val remainingCount: Int,
        val totalSections: Int,
        val isAllCleared: Boolean,
        val nextSectionId: Int?,
        val clearedSections: List<ClearedSectionInfo>
) {
    companion object {
        fun from(userProgress: UserProgress): UserProgressResponse {
            return UserProgressResponse(
                    userId = userProgress.userId.toString(),
                    progressPercentage = userProgress.calculateProgressPercentage(),
                    clearedCount = userProgress.getClearedCount(),
                    remainingCount = userProgress.getRemainingCount(),
                    totalSections = com.j15.backend.domain.model.section.Section.TOTAL_SECTIONS,
                    isAllCleared = userProgress.isAllCleared(),
                    nextSectionId = userProgress.suggestNextSection()?.value,
                    clearedSections =
                            userProgress.clearedSections.map {
                                ClearedSectionInfo(
                                        sectionId = it.sectionId.value,
                                        completedAt = it.completedAt
                                )
                            }
            )
        }
    }
}

data class ClearedSectionInfo(val sectionId: Int, val completedAt: Instant)
