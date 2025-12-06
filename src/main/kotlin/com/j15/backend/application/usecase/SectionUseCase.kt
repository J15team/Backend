package com.j15.backend.application.usecase

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.domain.repository.SubjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// セクション管理ユースケース（アプリケーション層）
@Service
@Transactional
class SectionUseCase(
        private val sectionRepository: SectionRepository,
        private val subjectRepository: SubjectRepository
) {

    /** 特定題材の全セクション一覧を取得 フロントエンドが利用可能な全セクション情報を返す */
    @Transactional(readOnly = true)
    fun getAllSections(subjectId: SubjectId): List<Section> {
        // 題材の存在確認
        if (!subjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")
        }
        return sectionRepository.findAllBySubjectId(subjectId)
    }

    /** 特定セクションの詳細を取得 */
    @Transactional(readOnly = true)
    fun getSectionById(subjectId: SubjectId, sectionId: SectionId): Section? {
        return sectionRepository.findById(subjectId, sectionId)
    }

    /** セクションが存在するかチェック */
    @Transactional(readOnly = true)
    fun existsSection(subjectId: SubjectId, sectionId: SectionId): Boolean {
        return sectionRepository.existsById(subjectId, sectionId)
    }

    /** セクションを作成（管理者用） */
    fun createSection(section: Section): Section {
        // 題材の存在確認
        if (!subjectRepository.existsById(section.subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${section.subjectId.value}")
        }
        return sectionRepository.save(section)
    }
}
