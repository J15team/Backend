package com.j15.backend.domain.model.section

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SectionIdTest {

    @Test
    fun `有効なセクションIDが作成できる`() {
        val sectionId0 = SectionId(0)
        assertEquals(0, sectionId0.value)

        val sectionId50 = SectionId(50)
        assertEquals(50, sectionId50.value)

        val sectionId100 = SectionId(100)
        assertEquals(100, sectionId100.value)
    }

    @Test
    fun `0未満のセクションIDは例外をスローする`() {
        val exception = assertThrows<IllegalArgumentException> { SectionId(-1) }
        assert(exception.message!!.contains("0~100"))
    }

    @Test
    fun `100を超えるセクションIDは例外をスローする`() {
        val exception = assertThrows<IllegalArgumentException> { SectionId(101) }
        assert(exception.message!!.contains("0~100"))
    }
}
