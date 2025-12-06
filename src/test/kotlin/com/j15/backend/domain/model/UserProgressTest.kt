package com.j15.backend.domain.model.progress

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserProgressTest {

    @Test
    fun `進捗率が正しく計算される`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(1)),
                        UserClearedSection.create(userId, SectionId(2))
                )

        val userProgress = UserProgress(userId, clearedSections)

        // 3/101 = 約2.97%
        assertEquals(2.9702970297029703, userProgress.calculateProgressPercentage(), 0.0001)
    }

    @Test
    fun `完了数と未完了数が正しく取得される`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(5)),
                        UserClearedSection.create(userId, SectionId(10))
                )

        val userProgress = UserProgress(userId, clearedSections)

        assertEquals(3, userProgress.getClearedCount())
        assertEquals(98, userProgress.getRemainingCount())
    }

    @Test
    fun `セクション完了済みの判定が正しく動作する`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(5))
                )

        val userProgress = UserProgress(userId, clearedSections)

        assertTrue(userProgress.isSectionCleared(SectionId(0)))
        assertTrue(userProgress.isSectionCleared(SectionId(5)))
        assertFalse(userProgress.isSectionCleared(SectionId(1)))
    }

    @Test
    fun `次に完了すべきセクションが正しく提案される`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(1)),
                        UserClearedSection.create(userId, SectionId(2))
                )

        val userProgress = UserProgress(userId, clearedSections)

        assertEquals(3, userProgress.suggestNextSection()?.value)
    }

    @Test
    fun `飛び飛びで完了した場合でも次のセクションは順番に提案される`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections =
                listOf(
                        UserClearedSection.create(userId, SectionId(0)),
                        UserClearedSection.create(userId, SectionId(5)),
                        UserClearedSection.create(userId, SectionId(10))
                )

        val userProgress = UserProgress(userId, clearedSections)

        assertEquals(1, userProgress.suggestNextSection()?.value)
    }

    @Test
    fun `全セクション完了時はnullが返される`() {
        val userId = UserId(UUID.randomUUID())
        val clearedSections = (0..100).map { UserClearedSection.create(userId, SectionId(it)) }

        val userProgress = UserProgress(userId, clearedSections)

        assertNull(userProgress.suggestNextSection())
        assertTrue(userProgress.isAllCleared())
        assertEquals(100.0, userProgress.calculateProgressPercentage(), 0.0001)
    }

    @Test
    fun `進捗0パーセントの場合`() {
        val userId = UserId(UUID.randomUUID())
        val userProgress = UserProgress(userId, emptyList())

        assertEquals(0.0, userProgress.calculateProgressPercentage())
        assertEquals(0, userProgress.getClearedCount())
        assertEquals(Section.TOTAL_SECTIONS, userProgress.getRemainingCount())
        assertEquals(0, userProgress.suggestNextSection()?.value)
        assertFalse(userProgress.isAllCleared())
    }
}
