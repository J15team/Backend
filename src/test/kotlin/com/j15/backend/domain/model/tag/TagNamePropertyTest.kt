package com.j15.backend.domain.model.tag

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

/**
 * Feature: subject-tags, Property 1: Tag Name Validation Validates: Requirements 1.2, 1.4, 1.5, 8.3
 */
class TagNamePropertyTest :
        FunSpec({

            // 有効な文字のみを含む文字列を生成するArbitrary
            val validChars =
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-あいうえおアイウエオ漢字"

            val validTagNameArb: Arb<String> =
                    Arb.int(1..50).map { length ->
                        (1..length).map { validChars.random() }.joinToString("")
                    }

            test("有効なタグ名は受け入れられる") {
                checkAll(100, validTagNameArb) { name: String ->
                    val tagName = TagName(name)
                    tagName.value shouldBe name
                }
            }

            test("空文字列は拒否される") { shouldThrow<IllegalArgumentException> { TagName("") } }

            test("空白のみの文字列は拒否される") {
                checkAll(100, Arb.int(1..10)) { length: Int ->
                    val whitespaceOnly = " ".repeat(length)
                    shouldThrow<IllegalArgumentException> { TagName(whitespaceOnly) }
                }
            }

            test("51文字以上のタグ名は拒否される") {
                checkAll(100, Arb.int(51..100)) { length: Int ->
                    val longName = "a".repeat(length)
                    shouldThrow<IllegalArgumentException> { TagName(longName) }
                }
            }

            test("1-50文字のタグ名は受け入れられる") {
                checkAll(100, Arb.int(1..50)) { length: Int ->
                    val name = "a".repeat(length)
                    val tagName = TagName(name)
                    tagName.value.length shouldBe length
                }
            }

            test("toDisplayString()は#プレフィックスを付ける") {
                checkAll(100, validTagNameArb) { name: String ->
                    val tagName = TagName(name)
                    tagName.toDisplayString() shouldBe "#$name"
                }
            }

            test("同じ値を持つTagNameは等価である") {
                checkAll(100, validTagNameArb) { name: String ->
                    val tagName1 = TagName(name)
                    val tagName2 = TagName(name)
                    tagName1 shouldBe tagName2
                }
            }
        })
