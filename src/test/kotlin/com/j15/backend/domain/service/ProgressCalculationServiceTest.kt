package com.j15.backend.domain.service

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProgressCalculationServiceTest {

    private val service = ProgressCalculationService()

    @Test
    fun `ユーザー進捗状態を構築できる`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(1))
                )

        val userProgress = service.buildUserProgress(userId, clearedSections)

        assertEquals(userId, userProgress.userId)
        assertEquals(2, userProgress.getClearedCount())
    }

    @Test
    fun `重複完了の検証が機能する`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections = listOf(UserClearedSection.create(userId, SectionId(0)))
        val userProgress = service.buildUserProgress(userId, clearedSections)

        val result = service.validateNoDuplicate(userProgress, SectionId(0))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message!!.contains("既に完了"))
    }

    @Test
    fun `未完了セクションの検証は成功する`() {
        val userId = UserId(UUID.randomUUID())
        val userProgress = service.buildUserProgress(userId, emptyList())

        val result = service.validateNoDuplicate(userProgress, SectionId(0))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `有効なセクションIDの検証は成功する`() {
        val result0 = service.validateSectionId(SectionId(0))
        assertTrue(result0.isSuccess)

        val result50 = service.validateSectionId(SectionId(50))
        assertTrue(result50.isSuccess)

        val result100 = service.validateSectionId(SectionId(100))
        assertTrue(result100.isSuccess)
    }
}
