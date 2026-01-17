package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.*
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.assignment.AssignmentSectionRepository
import com.j15.backend.domain.repository.assignment.SubmissionRepository
import com.j15.backend.infrastructure.client.JudgeResultDto
import com.j15.backend.infrastructure.client.JudgeServiceClient

/** テスト用モックJudgeServiceClient */
class TestMockJudgeServiceClient : JudgeServiceClient {
    override fun judge(
            code: String,
            language: Language,
            testCases: List<TestCase>,
            timeLimit: Int,
            memoryLimit: Int
    ): List<JudgeResultDto> {
        return testCases.mapIndexed { index, _ ->
            JudgeResultDto(index = index, verdict = Verdict.AC, executionTime = 100)
        }
    }
}

/** テスト用モックSubmissionRepository */
class TestMockSubmissionRepository : SubmissionRepository {
    override fun findById(id: SubmissionId) = null
    override fun findByUserAndSection(
            userId: UserId,
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ) = emptyList<Submission>()
    override fun findByUserAndSubject(userId: UserId, subjectId: AssignmentSubjectId) =
            emptyList<Submission>()
    override fun findBySection(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) =
            emptyList<Submission>()
    override fun findBySubject(subjectId: AssignmentSubjectId) = emptyList<Submission>()
    override fun findAll() = emptyList<Submission>()
    override fun save(submission: Submission) = submission
}

/** テスト用モックAssignmentSectionRepository */
class TestMockAssignmentSectionRepository : AssignmentSectionRepository {
    override fun findById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) = null
    override fun findBySubjectId(subjectId: AssignmentSubjectId) = emptyList<AssignmentSection>()
    override fun existsById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) = false
    override fun save(section: AssignmentSection) = section
    override fun deleteById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) {}
    override fun deleteAllBySubjectId(subjectId: AssignmentSubjectId) {}
}

/** 課題ありセクションを返すモックリポジトリ */
class TestMockAssignmentSectionRepositoryWithAssignment : AssignmentSectionRepository {
    override fun findById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) =
            AssignmentSection(
                    assignmentSubjectId = subjectId,
                    sectionId = sectionId,
                    title = "Test Section",
                    hasAssignment = true,
                    testCases = """[{"input":"","expected":"Hello\n","visible":true}]""",
                    timeLimit = 2000,
                    memoryLimit = 256
            )
    override fun findBySubjectId(subjectId: AssignmentSubjectId) = emptyList<AssignmentSection>()
    override fun existsById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) = true
    override fun save(section: AssignmentSection) = section
    override fun deleteById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) {}
    override fun deleteAllBySubjectId(subjectId: AssignmentSubjectId) {}
}

/** 保存回数を追跡するモックリポジトリ */
class TestTrackingSubmissionRepository : SubmissionRepository {
    private var _savedCount = 0
    val savedCount: Int
        get() = _savedCount

    fun clear() {
        _savedCount = 0
    }

    override fun findById(id: SubmissionId) = null
    override fun findByUserAndSection(
            userId: UserId,
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ) = emptyList<Submission>()
    override fun findByUserAndSubject(userId: UserId, subjectId: AssignmentSubjectId) =
            emptyList<Submission>()
    override fun findBySection(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) =
            emptyList<Submission>()
    override fun findBySubject(subjectId: AssignmentSubjectId) = emptyList<Submission>()
    override fun findAll() = emptyList<Submission>()
    override fun save(submission: Submission): Submission {
        _savedCount++
        return submission.copy(id = SubmissionId(_savedCount.toLong()))
    }
}

/** テスト用インメモリリポジトリ */
class TestInMemorySubmissionRepository : SubmissionRepository {
    private val submissions = mutableListOf<Submission>()

    fun addSubmission(submission: Submission) {
        submissions.add(submission)
    }

    override fun findById(id: SubmissionId): Submission? {
        return submissions.find { it.id == id }
    }

    override fun findByUserAndSection(
            userId: UserId,
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission> {
        return submissions
                .filter {
                    it.userId == userId &&
                            it.assignmentSubjectId == subjectId &&
                            it.sectionId == sectionId
                }
                .sortedByDescending { it.submittedAt }
    }

    override fun findByUserAndSubject(
            userId: UserId,
            subjectId: AssignmentSubjectId
    ): List<Submission> {
        return submissions
                .filter { it.userId == userId && it.assignmentSubjectId == subjectId }
                .sortedByDescending { it.submittedAt }
    }

    override fun findBySection(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission> {
        return submissions
                .filter { it.assignmentSubjectId == subjectId && it.sectionId == sectionId }
                .sortedByDescending { it.submittedAt }
    }

    override fun findBySubject(subjectId: AssignmentSubjectId): List<Submission> {
        return submissions.filter { it.assignmentSubjectId == subjectId }.sortedByDescending {
            it.submittedAt
        }
    }

    override fun findAll(): List<Submission> {
        return submissions.sortedByDescending { it.submittedAt }
    }

    override fun save(submission: Submission): Submission {
        submissions.add(submission)
        return submission
    }
}
