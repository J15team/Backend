package com.j15.backend.domain.model.assignment

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: assignment-execution-system, Property 2: テストケース JSON バリデーション Validates: Requirements
 * 2.1, 2.2, 2.5
 *
 * For any テストケース JSON, 各要素は input、expected、visible フィールドを持ち、 不正な形式は拒否される
 */
class TestCasePropertyTest :
        FunSpec({
            val validInputArb: Arb<String> = Arb.string(0..100)
            val validExpectedArb: Arb<String> = Arb.string(1..100).map { it + "\n" }

            test("有効なテストケースJSONはパースできる") {
                checkAll(100, validInputArb, validExpectedArb) { input, expected ->
                    val testCase = TestCase(input = input, expected = expected, visible = true)
                    val json = TestCase.toJson(listOf(testCase))

                    val parsed = TestCase.parseFromJson(json)

                    parsed.size shouldBe 1
                    parsed[0].input shouldBe input
                    parsed[0].expected shouldBe expected
                    parsed[0].visible shouldBe true
                }
            }

            test("複数のテストケースを含むJSONはパースできる") {
                checkAll(100, Arb.int(1..10)) { count ->
                    val testCases =
                            (0 until count).map { i ->
                                TestCase(
                                        input = "input_$i",
                                        expected = "expected_$i\n",
                                        visible = i == 0 // 最初のケースは可視
                                )
                            }
                    val json = TestCase.toJson(testCases)

                    val parsed = TestCase.parseFromJson(json)

                    parsed.size shouldBe count
                    parsed.forEachIndexed { index, tc ->
                        tc.input shouldBe "input_$index"
                        tc.expected shouldBe "expected_$index\n"
                    }
                }
            }

            test("可視テストケースと非可視テストケースを混在できる") {
                checkAll(100, Arb.boolean()) { firstVisible ->
                    val testCases =
                            listOf(
                                    TestCase(
                                            input = "1",
                                            expected = "1\n",
                                            visible = true
                                    ), // 少なくとも1つは可視
                                    TestCase(input = "2", expected = "2\n", visible = firstVisible)
                            )
                    val json = TestCase.toJson(testCases)

                    val parsed = TestCase.parseFromJson(json)

                    parsed.size shouldBe 2
                    parsed.any { it.visible } shouldBe true
                }
            }

            test("空のテストケース配列は拒否される") {
                shouldThrow<TestCaseParseException> { TestCase.parseFromJson("[]") }
            }

            test("可視テストケースが1つもない場合は拒否される") {
                val json = """[{"input":"1","expected":"1\n","visible":false}]"""

                shouldThrow<TestCaseParseException> { TestCase.parseFromJson(json) }
            }

            test("不正なJSON形式は拒否される") {
                shouldThrow<TestCaseParseException> { TestCase.parseFromJson("invalid json") }
            }

            test("必須フィールドが欠けている場合は拒否される") {
                // inputフィールドが欠けている
                shouldThrow<TestCaseParseException> {
                    TestCase.parseFromJson("""[{"expected":"1\n","visible":true}]""")
                }

                // expectedフィールドが欠けている
                shouldThrow<TestCaseParseException> {
                    TestCase.parseFromJson("""[{"input":"1","visible":true}]""")
                }

                // visibleフィールドが欠けている
                shouldThrow<TestCaseParseException> {
                    TestCase.parseFromJson("""[{"input":"1","expected":"1\n"}]""")
                }
            }

            test("ラウンドトリップ: パース→シリアライズ→パースで同じ結果になる") {
                checkAll(100, validInputArb, validExpectedArb, Arb.boolean()) {
                        input,
                        expected,
                        visible ->
                    val original =
                            listOf(
                                    TestCase(input = input, expected = expected, visible = true),
                                    TestCase(
                                            input = "hidden",
                                            expected = "hidden\n",
                                            visible = visible
                                    )
                            )

                    val json1 = TestCase.toJson(original)
                    val parsed1 = TestCase.parseFromJson(json1)
                    val json2 = TestCase.toJson(parsed1)
                    val parsed2 = TestCase.parseFromJson(json2)

                    parsed1 shouldBe parsed2
                }
            }
        })
