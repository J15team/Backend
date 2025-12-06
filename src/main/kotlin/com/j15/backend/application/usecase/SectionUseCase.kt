package com.j15.backend.application.usecase

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.repository.SectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// セクション管理ユースケース（アプリケーション層）
@Service
@Transactional
class SectionUseCase(private val sectionRepository: SectionRepository) {

    /** 全セクション一覧を取得 フロントエンドが利用可能な全セクション情報を返す */
    @Transactional(readOnly = true)
    fun getAllSections(): List<Section> {
        return sectionRepository.findAll()
    }

    /** 特定セクションの詳細を取得 */
    @Transactional(readOnly = true)
    fun getSectionById(sectionId: SectionId): Section? {
        return sectionRepository.findById(sectionId)
    }

    /** セクションが存在するかチェック */
    @Transactional(readOnly = true)
    fun existsSection(sectionId: SectionId): Boolean {
        return sectionRepository.existsById(sectionId)
    }

    /** セクションを作成（管理者用） */
    fun createSection(section: Section): Section {
        return sectionRepository.save(section)
    }
}
