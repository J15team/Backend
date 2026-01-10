package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AdminUseCase
import com.j15.backend.domain.model.user.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.springframework.http.HttpStatus

/**
 * Feature: developer-role Property 6: Admin間操作の許可 Validates: Requirements 7.3
 *
 * AdminControllerでのDeveloper保護テスト
 */
class AdminControllerProtectionTest :
        FunSpec({
            val adminUseCase = mockk<AdminUseCase>()
            val controller = AdminController(adminUseCase)

            beforeTest { clearMocks(adminUseCase) }

            /**
             * Property 6: Admin間操作の許可 For any ROLE_ADMIN user managing another ROLE_ADMIN user, the
             * operation should succeed when proper authentication is provided.
             */
            context("Property 6: Admin間操作の許可") {
                test("ROLE_ADMINユーザーの更新は成功する") {
                    val adminId = UUID.randomUUID()
                    val adminUser = createTestUser(adminId, UserRole.ROLE_ADMIN)
                    val updateResult = AdminUseCase.AdminUserCreationResult(adminUser)

                    every { adminUseCase.updateAdminUser(any(), any(), any(), any()) } returns
                            updateResult

                    val response =
                            controller.updateAdminUser(
                                    adminId.toString(),
                                    com.j15.backend.presentation.dto.request.AdminUserUpdateRequest(
                                            email = "new@example.com",
                                            username = null,
                                            password = null
                                    )
                            )

                    response.statusCode shouldBe HttpStatus.OK
                }

                test("ROLE_ADMINユーザーの削除は成功する") {
                    val adminId = UUID.randomUUID()

                    every { adminUseCase.deleteAdminUser(any()) } returns Unit

                    val response = controller.deleteAdminUser(adminId.toString())

                    response.statusCode shouldBe HttpStatus.NO_CONTENT
                }

                test("ROLE_DEVELOPERユーザーの更新はSecurityExceptionで失敗する") {
                    val developerId = UUID.randomUUID()

                    every { adminUseCase.updateAdminUser(any(), any(), any(), any()) } throws
                            SecurityException("開発者ユーザーは更新できません")

                    val response =
                            controller.updateAdminUser(
                                    developerId.toString(),
                                    com.j15.backend.presentation.dto.request.AdminUserUpdateRequest(
                                            email = "new@example.com",
                                            username = null,
                                            password = null
                                    )
                            )

                    // SecurityExceptionはcatchされてINTERNAL_SERVER_ERRORになる
                    // （AdminControllerはSecurityExceptionを明示的にハンドルしていない）
                    response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }

                test("ROLE_DEVELOPERユーザーの削除はSecurityExceptionで失敗する") {
                    val developerId = UUID.randomUUID()

                    every { adminUseCase.deleteAdminUser(any()) } throws
                            SecurityException("開発者ユーザーは削除できません")

                    val response = controller.deleteAdminUser(developerId.toString())

                    // SecurityExceptionはcatchされてINTERNAL_SERVER_ERRORになる
                    response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                }
            }

            context("Admin一覧取得") {
                test("getAllAdminUsersはROLE_ADMINのみを返す") {
                    val admins =
                            listOf(
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_ADMIN),
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_ADMIN)
                            )

                    every { adminUseCase.getAllAdminUsers() } returns admins

                    val response = controller.getAllAdminUsers()

                    response.statusCode shouldBe HttpStatus.OK
                    response.body?.admins?.size shouldBe 2
                }
            }
        })

private fun createTestUser(userId: UUID, role: UserRole): User {
    return User(
            userId = UserId(userId),
            username = Username("testuser_${userId.toString().take(8)}"),
            email = Email("test_${userId.toString().take(8)}@example.com"),
            passwordHash = PasswordHash("hashedpassword"),
            role = role
    )
}
