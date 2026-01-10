package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.DeveloperUseCase
import com.j15.backend.domain.model.user.*
import com.j15.backend.presentation.dto.response.EndpointInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus

/** Feature: developer-role DeveloperControllerの統合テスト */
class DeveloperControllerTest :
        FunSpec({
            val developerUseCase = mockk<DeveloperUseCase>()
            val controller = DeveloperController(developerUseCase)

            beforeTest { clearMocks(developerUseCase) }

            /** 8.1 /api/dev/verify エンドポイントのテスト Requirements: 4.2, 4.3 */
            context("GET /api/dev/verify") {
                test("有効なX-Dev-Keyで { isDeveloper: true } を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("valid-key") } returns true

                    val response = controller.verifyDeveloper("valid-key")

                    response.statusCode shouldBe HttpStatus.OK
                    response.body?.isDeveloper shouldBe true
                }

                test("無効なX-Dev-Keyで { isDeveloper: false } を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("invalid-key") } returns false

                    val response = controller.verifyDeveloper("invalid-key")

                    response.statusCode shouldBe HttpStatus.OK
                    response.body?.isDeveloper shouldBe false
                }

                test("X-Dev-Key未設定で { isDeveloper: false } を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true

                    val response = controller.verifyDeveloper(null)

                    response.statusCode shouldBe HttpStatus.OK
                    response.body?.isDeveloper shouldBe false
                }

                test("開発者機能が無効の場合 { isDeveloper: false } を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns false

                    val response = controller.verifyDeveloper("any-key")

                    response.statusCode shouldBe HttpStatus.OK
                    response.body?.isDeveloper shouldBe false
                }
            }

            /** 8.2 /api/dev/endpoints エンドポイントのテスト Requirements: 5.2, 5.4 */
            context("GET /api/dev/endpoints") {
                test("有効なX-Dev-Keyでエンドポイント一覧を返す") {
                    val endpoints =
                            listOf(
                                    EndpointInfo("GET", "/api/health", "HealthController.health()"),
                                    EndpointInfo(
                                            "POST",
                                            "/api/auth/signin",
                                            "AuthController.signin()"
                                    )
                            )

                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("valid-key") } returns true
                    every { developerUseCase.getAllEndpoints() } returns endpoints

                    val response = controller.getEndpoints("valid-key")

                    response.statusCode shouldBe HttpStatus.OK
                }

                test("無効なX-Dev-Keyで401を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("invalid-key") } returns false

                    val response = controller.getEndpoints("invalid-key")

                    response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                }

                test("X-Dev-Key未設定で401を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true

                    val response = controller.getEndpoints(null)

                    response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                }

                test("開発者機能が無効の場合503を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns false

                    val response = controller.getEndpoints("any-key")

                    response.statusCode shouldBe HttpStatus.SERVICE_UNAVAILABLE
                }
            }

            /**
             * 8.3 /api/dev/users/{userId} エンドポイントのテスト Property 3: ユーザー削除のロール制限 Validates:
             * Requirements 6.2, 6.5
             */
            context("DELETE /api/dev/users/{userId}") {
                test("有効なX-Dev-KeyでROLE_USERを削除できる") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("valid-key") } returns true
                    every { developerUseCase.deleteUser(any()) } returns true

                    val response = controller.deleteUser("user-id", "valid-key")

                    response.statusCode shouldBe HttpStatus.NO_CONTENT
                }

                test("ROLE_DEVELOPERの削除は403を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("valid-key") } returns true
                    every { developerUseCase.deleteUser(any()) } throws
                            SecurityException("開発者ユーザーは削除できません")

                    val response = controller.deleteUser("developer-id", "valid-key")

                    response.statusCode shouldBe HttpStatus.FORBIDDEN
                }

                test("存在しないユーザーの削除は404を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("valid-key") } returns true
                    every { developerUseCase.deleteUser(any()) } throws
                            IllegalArgumentException("ユーザーが見つかりません")

                    val response = controller.deleteUser("nonexistent-id", "valid-key")

                    response.statusCode shouldBe HttpStatus.NOT_FOUND
                }

                test("無効なX-Dev-Keyで401を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true
                    every { developerUseCase.validateDevKey("invalid-key") } returns false

                    val response = controller.deleteUser("user-id", "invalid-key")

                    response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                }

                test("X-Dev-Key未設定で401を返す") {
                    every { developerUseCase.isDevModeEnabled() } returns true

                    val response = controller.deleteUser("user-id", null)

                    response.statusCode shouldBe HttpStatus.UNAUTHORIZED
                }
            }
        })
