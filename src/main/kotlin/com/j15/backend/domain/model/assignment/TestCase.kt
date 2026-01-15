package com.j15.backend.domain.model.assignment

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

/** テストケースデータクラス（JSON パース用） */
data class TestCase(
        @JsonProperty("input") val input: String,
        @JsonProperty("expected") val expected: String,
        @JsonProperty("visible") val visible: Boolean
) {
    companion object {
        private val objectMapper = jacksonObjectMapper()

        /**
         * JSON文字列からテストケースリストをパース
         * @throws TestCaseParseException パースに失敗した場合
         */
        fun parseFromJson(json: String): List<TestCase> {
            return try {
                val testCases: List<TestCase> = objectMapper.readValue(json)
                validateTestCases(testCases)
                testCases
            } catch (e: JsonProcessingException) {
                throw TestCaseParseException("テストケースのJSON形式が不正です: ${e.message}")
            }
        }

        /** テストケースリストをJSON文字列に変換 */
        fun toJson(testCases: List<TestCase>): String {
            return objectMapper.writeValueAsString(testCases)
        }

        /** テストケースのバリデーション */
        private fun validateTestCases(testCases: List<TestCase>) {
            if (testCases.isEmpty()) {
                throw TestCaseParseException("テストケースは1つ以上必要です")
            }

            // 少なくとも1つの可視テストケースが必要
            if (testCases.none { it.visible }) {
                throw TestCaseParseException("少なくとも1つの可視テストケースが必要です")
            }
        }
    }
}

/** テストケースパース例外 */
class TestCaseParseException(message: String) : RuntimeException(message)
