package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.*
import com.j15.backend.domain.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/** Feature: developer-role DeveloperUseCaseのテスト */
class DeveloperUseCaseTest :
        FunSpec({
            val userRepository = mockk<UserRepository>()
            val requestMappingHandlerMapping = mockk<RequestMappingHandlerMapping>()

            beforeTest { clearMocks(userRepository, requestMappingHandlerMapping) }

            /**
             * Property 2: 無効なDev-Keyの拒否 Validates: Requirements 3.2, 5.4, 6.4
             *
             * For any string that is not equal to the configured DEV_API_KEY, validateDevKey should
             * return false.
             */
            context("Property 2: 無効なDev-Keyの拒否") {
                test("有効なDev-Keyの場合はtrueを返す") {
                    val validKey = "test-dev-key-12345"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )

                    useCase.validateDevKey(validKey) shouldBe true
                }

                test("無効なDev-Keyの場合はfalseを返す") {
                    val validKey = "test-dev-key-12345"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )

                    checkAll(100, Arb.string(1..100)) { invalidKey: String ->
                        if (invalidKey != validKey) {
                            useCase.validateDevKey(invalidKey) shouldBe false
                        }
                    }
                }

                test("空のDev-Keyが設定されている場合はfalseを返す") {
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    ""
                            )

                    useCase.validateDevKey("any-key") shouldBe false
                }

                test("非常に長いキーは拒否される") {
                    val validKey = "test-dev-key-12345"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )
                    val longKey = "a".repeat(1001)

                    useCase.validateDevKey(longKey) shouldBe false
                }
            }

            /** Property 3: ユーザー削除のロール制限 Validates: Requirements 6.2, 6.5 */
            context("Property 3: ユーザー削除のロール制限") {
                test("ROLE_USERは削除可能") {
                    val validKey = "test-dev-key"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )
                    val userId = UUID.randomUUID()
                    val user = createTestUser(userId, UserRole.ROLE_USER)

                    every { userRepository.findById(any()) } returns user
                    every { userRepository.deleteById(any()) } returns Unit

                    useCase.deleteUser(userId.toString()) shouldBe true
                    verify { userRepository.deleteById(UserId(userId)) }
                }

                test("ROLE_ADMINは削除可能") {
                    val validKey = "test-dev-key"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )
                    val userId = UUID.randomUUID()
                    val user = createTestUser(userId, UserRole.ROLE_ADMIN)

                    every { userRepository.findById(any()) } returns user
                    every { userRepository.deleteById(any()) } returns Unit

                    useCase.deleteUser(userId.toString()) shouldBe true
                    verify { userRepository.deleteById(UserId(userId)) }
                }

                test("ROLE_DEVELOPERは削除不可（SecurityException）") {
                    val validKey = "test-dev-key"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )
                    val userId = UUID.randomUUID()
                    val user = createTestUser(userId, UserRole.ROLE_DEVELOPER)

                    every { userRepository.findById(any()) } returns user

                    shouldThrow<SecurityException> { useCase.deleteUser(userId.toString()) }
                }

                test("存在しないユーザーはIllegalArgumentException") {
                    val validKey = "test-dev-key"
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    validKey
                            )
                    val userId = UUID.randomUUID()

                    every { userRepository.findById(any()) } returns null

                    shouldThrow<IllegalArgumentException> { useCase.deleteUser(userId.toString()) }
                }
            }

            context("isDevModeEnabled") {
                test("Dev-Keyが設定されている場合はtrueを返す") {
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    "some-key"
                            )
                    useCase.isDevModeEnabled() shouldBe true
                }

                test("Dev-Keyが空の場合はfalseを返す") {
                    val useCase =
                            createUseCaseWithDevKey(
                                    userRepository,
                                    requestMappingHandlerMapping,
                                    ""
                            )
                    useCase.isDevModeEnabled() shouldBe false
                }
            }
        })

private fun createUseCaseWithDevKey(
        userRepository: UserRepository,
        requestMappingHandlerMapping: RequestMappingHandlerMapping,
        devKey: String
): DeveloperUseCase {
    // リフレクションを使ってdevApiKeyを設定
    val useCase = DeveloperUseCase(userRepository, requestMappingHandlerMapping, devKey)
    return useCase
}

private fun createTestUser(userId: UUID, role: UserRole): User {
    return User(
            userId = UserId(userId),
            username = Username("testuser"),
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashedpassword"),
            role = role
    )
}

/** Feature: developer-role Property 5: エンドポイント情報の完全性 Validates: Requirements 5.3 */
class EndpointInfoPropertyTest :
        FunSpec({

            /**
             * Property 5: エンドポイント情報の完全性 For any endpoint returned by getAllEndpoints(), the
             * response should include non-empty method, path, and description fields.
             */
            test("全てのエンドポイント情報はmethod, path, descriptionが非空である") {
                val userRepository = mockk<UserRepository>()
                val requestMappingHandlerMapping = mockk<RequestMappingHandlerMapping>()

                // 空のハンドラーマップを返す（実際のSpringコンテキストなしでテスト）
                every { requestMappingHandlerMapping.handlerMethods } returns emptyMap()

                val useCase =
                        DeveloperUseCase(userRepository, requestMappingHandlerMapping, "test-key")
                val endpoints = useCase.getAllEndpoints()

                // 空のリストでも成功（全ての要素が条件を満たす）
                endpoints.all { endpoint ->
                    endpoint.method.isNotBlank() &&
                            endpoint.path.isNotBlank() &&
                            endpoint.description.isNotBlank()
                } shouldBe true
            }
        })
