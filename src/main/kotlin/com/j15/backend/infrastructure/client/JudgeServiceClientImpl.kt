package com.j15.backend.infrastructure.client

import com.j15.backend.domain.model.assignment.Language
import com.j15.backend.domain.model.assignment.TestCase
import com.j15.backend.domain.model.assignment.Verdict
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/** Judge ServiceクライアントHTTP実装 */
@Component
@EnableConfigurationProperties(JudgeServiceProperties::class)
class JudgeServiceClientImpl(
        private val properties: JudgeServiceProperties,
        webClientBuilder: WebClient.Builder
) : JudgeServiceClient {

    private val logger = LoggerFactory.getLogger(JudgeServiceClientImpl::class.java)

    private val webClient: WebClient = webClientBuilder.baseUrl(properties.baseUrl).build()

    override fun judge(
            code: String,
            language: Language,
            testCases: List<TestCase>,
            timeLimit: Int,
            memoryLimit: Int
    ): List<JudgeResultDto> {
        val request =
                JudgeRequest(
                        code = code,
                        language = language.name.lowercase(),
                        testCases =
                                testCases.map { tc ->
                                    JudgeTestCase(input = tc.input, expected = tc.expected)
                                },
                        timeLimit = timeLimit,
                        memoryLimit = memoryLimit
                )

        return try {
            val response =
                    webClient
                            .post()
                            .uri("/api/judge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(JudgeResponse::class.java)
                            .block(Duration.ofMillis(properties.timeout))

            response?.results?.mapIndexed { index, result ->
                JudgeResultDto(
                        index = index,
                        verdict = Verdict.valueOf(result.verdict.uppercase()),
                        executionTime = result.executionTime,
                        memoryUsed = result.memoryUsed,
                        actualOutput = result.actualOutput,
                        errorMessage = result.errorMessage
                )
            }
                    ?: emptyList()
        } catch (e: WebClientResponseException) {
            logger.error("Judge Service error: ${e.statusCode} - ${e.responseBodyAsString}")
            throw JudgeServiceUnavailableException("Judge Serviceからエラーが返されました: ${e.statusCode}")
        } catch (e: Exception) {
            logger.error("Judge Service connection error", e)
            throw JudgeServiceUnavailableException("Judge Serviceに接続できません")
        }
    }
}

/** Judge Serviceリクエスト */
private data class JudgeRequest(
        val code: String,
        val language: String,
        val testCases: List<JudgeTestCase>,
        val timeLimit: Int,
        val memoryLimit: Int
)

/** Judge Serviceテストケース */
private data class JudgeTestCase(val input: String, val expected: String)

/** Judge Serviceレスポンス */
private data class JudgeResponse(val results: List<JudgeResultItem>)

/** Judge Service結果アイテム */
private data class JudgeResultItem(
        val verdict: String,
        val executionTime: Int? = null,
        val memoryUsed: Int? = null,
        val actualOutput: String? = null,
        val errorMessage: String? = null
)

/** Judge Service利用不可例外 */
class JudgeServiceUnavailableException(message: String) : RuntimeException(message)
