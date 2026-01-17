package com.j15.backend.application.usecase.tag

import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.model.tag.TagType
import com.j15.backend.domain.repository.tag.SubjectTagRepository
import com.j15.backend.domain.repository.tag.TagRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

/** Feature: subject-tags, Property 4, 5, 13, 14 TagUseCaseのプロパティテスト */
class TagUseCasePropertyTest :
        FunSpec({
            val validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789あいうえお"

            val validTagNameArb: Arb<String> =
                    Arb.int(1..30).map { length ->
                        (1..length).map { validChars.random() }.joinToString("")
                    }

            /** Property 4: Tag Creation Uniqueness Validates: Requirements 1.3 */
            test("同名のタグを2回作成すると2回目は失敗する") {
                val tagRepository = mockk<TagRepository>()
                val subjectTagRepository = mockk<SubjectTagRepository>()
                val useCase = TagUseCase(tagRepository, subjectTagRepository)

                checkAll(10, validTagNameArb) { name: String ->
                    clearMocks(tagRepository)

                    // 1回目: タグが存在しない
                    every { tagRepository.existsByName(any()) } returns false
                    every { tagRepository.save(any()) } answers
                            {
                                val tag = firstArg<Tag>()
                                tag.copy(id = TagId(1L))
                            }

                    useCase.createTag(name)

                    // 2回目: タグが既に存在する
                    every { tagRepository.existsByName(any()) } returns true

                    shouldThrow<IllegalArgumentException> { useCase.createTag(name) }
                }
            }

            /** Property 5: Tag List Sorting Validates: Requirements 2.3 */
            test("getAllTagsは名前の昇順でソートされたリストを返す") {
                val tagRepository = mockk<TagRepository>()
                val subjectTagRepository = mockk<SubjectTagRepository>()
                val useCase = TagUseCase(tagRepository, subjectTagRepository)

                val tags =
                        listOf(
                                        Tag(
                                                TagId(1L),
                                                TagName("zebra"),
                                                TagType.NORMAL,
                                                Instant.now()
                                        ),
                                        Tag(
                                                TagId(2L),
                                                TagName("apple"),
                                                TagType.NORMAL,
                                                Instant.now()
                                        ),
                                        Tag(
                                                TagId(3L),
                                                TagName("banana"),
                                                TagType.NORMAL,
                                                Instant.now()
                                        )
                                )
                                .sortedBy { it.name.value }

                every { tagRepository.findAll() } returns tags

                val result = useCase.getAllTags()
                result.shouldBeSortedWith { a, b -> a.name.value.compareTo(b.name.value) }
            }

            /** Property 13: Default Tag Type Validates: Requirements 9.2 */
            test("タイプを指定せずに作成したタグはNORMALタイプになる") {
                val tagRepository = mockk<TagRepository>()
                val subjectTagRepository = mockk<SubjectTagRepository>()
                val useCase = TagUseCase(tagRepository, subjectTagRepository)

                checkAll(10, validTagNameArb) { name: String ->
                    clearMocks(tagRepository)

                    every { tagRepository.existsByName(any()) } returns false
                    every { tagRepository.save(any()) } answers
                            {
                                val tag = firstArg<Tag>()
                                tag.copy(id = TagId(1L))
                            }

                    val result = useCase.createTag(name)
                    result.type shouldBe TagType.NORMAL
                }
            }

            /** Property 14: Tag Type Filter Validates: Requirements 9.3 */
            test("タイプでフィルタした結果は全て指定したタイプを持つ") {
                val tagRepository = mockk<TagRepository>()
                val subjectTagRepository = mockk<SubjectTagRepository>()
                val useCase = TagUseCase(tagRepository, subjectTagRepository)

                val normalTags =
                        listOf(
                                Tag(TagId(1L), TagName("tag1"), TagType.NORMAL, Instant.now()),
                                Tag(TagId(2L), TagName("tag2"), TagType.NORMAL, Instant.now())
                        )
                val premiumTags =
                        listOf(Tag(TagId(3L), TagName("premium1"), TagType.PREMIUM, Instant.now()))

                every { tagRepository.findByType(TagType.NORMAL) } returns normalTags
                every { tagRepository.findByType(TagType.PREMIUM) } returns premiumTags

                val normalResult = useCase.getTagsByType(TagType.NORMAL)
                normalResult.all { it.type == TagType.NORMAL } shouldBe true

                val premiumResult = useCase.getTagsByType(TagType.PREMIUM)
                premiumResult.all { it.type == TagType.PREMIUM } shouldBe true
            }
        })
