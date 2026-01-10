package com.j15.backend.domain.model.tag

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import java.time.Instant

/** Feature: subject-tags, Property 2: Tag Name Equality Validates: Requirements 8.2 */
class TagPropertyTest :
        FunSpec({
            val validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-あいうえお"

            val validTagNameArb: Arb<String> =
                    Arb.int(1..50).map { length ->
                        (1..length).map { validChars.random() }.joinToString("")
                    }

            val positiveIdArb: Arb<Long> = Arb.long(1L..Long.MAX_VALUE)

            test("同じ名前を持つTagは等価である") {
                checkAll(100, validTagNameArb, positiveIdArb, positiveIdArb) {
                        name: String,
                        id1: Long,
                        id2: Long ->
                    val tag1 =
                            Tag(
                                    id = TagId(id1),
                                    name = TagName(name),
                                    type = TagType.NORMAL,
                                    createdAt = Instant.now()
                            )
                    val tag2 =
                            Tag(
                                    id = TagId(id2),
                                    name = TagName(name),
                                    type = TagType.PREMIUM,
                                    createdAt = Instant.now().minusSeconds(1000)
                            )
                    tag1 shouldBe tag2
                }
            }

            test("異なる名前を持つTagは等価でない") {
                checkAll(100, validTagNameArb, validTagNameArb, positiveIdArb) {
                        name1: String,
                        name2: String,
                        id: Long ->
                    if (name1 != name2) {
                        val tag1 = Tag(id = TagId(id), name = TagName(name1), type = TagType.NORMAL)
                        val tag2 = Tag(id = TagId(id), name = TagName(name2), type = TagType.NORMAL)
                        tag1 shouldNotBe tag2
                    }
                }
            }

            test("同じ名前を持つTagは同じhashCodeを持つ") {
                checkAll(100, validTagNameArb, positiveIdArb, positiveIdArb) {
                        name: String,
                        id1: Long,
                        id2: Long ->
                    val tag1 = Tag(id = TagId(id1), name = TagName(name))
                    val tag2 = Tag(id = TagId(id2), name = TagName(name))
                    tag1.hashCode() shouldBe tag2.hashCode()
                }
            }
        })
