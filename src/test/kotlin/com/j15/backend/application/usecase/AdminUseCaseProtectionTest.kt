package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.*
import com.j15.backend.domain.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.springframework.security.crypto.password.PasswordEncoder

/** Feature: developer-role AdminUseCaseの開発者保護テスト */
class AdminUseCaseProtectionTest :
        FunSpec({
            val userRepository = mockk<UserRepository>()
            val passwordEncoder = mockk<PasswordEncoder>()

            beforeTest { clearMocks(userRepository, passwordEncoder) }

            /**
             * Property 1: 開発者保護の不変性 Validates: Requirements 2.1, 2.2, 2.3, 2.4, 7.2
             *
             * For any ROLE_DEVELOPER user and for any non-DEVELOPER user attempting to modify or
             * delete them, the operation should be rejected with SecurityException.
             */
            context("Property 1: 開発者保護の不変性") {
                test("ROLE_DEVELOPERユーザーの更新は拒否される") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)
                    val developerId = UUID.randomUUID()
                    val developerUser = createTestUser(developerId, UserRole.ROLE_DEVELOPER)

                    every { userRepository.findById(any()) } returns developerUser

                    shouldThrow<SecurityException> {
                        useCase.updateAdminUser(
                                userId = developerId.toString(),
                                email = "new@example.com",
                                username = null,
                                password = null
                        )
                    }
                }

                test("ROLE_DEVELOPERユーザーの削除は拒否される") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)
                    val developerId = UUID.randomUUID()
                    val developerUser = createTestUser(developerId, UserRole.ROLE_DEVELOPER)

                    every { userRepository.findById(any()) } returns developerUser

                    shouldThrow<SecurityException> {
                        useCase.deleteAdminUser(developerId.toString())
                    }
                }

                test("ROLE_ADMINユーザーの更新は成功する") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)
                    val adminId = UUID.randomUUID()
                    val adminUser = createTestUser(adminId, UserRole.ROLE_ADMIN)

                    every { userRepository.findById(any()) } returns adminUser
                    every { userRepository.existsByEmail(any()) } returns false
                    every { userRepository.existsByUsername(any()) } returns false
                    every { userRepository.save(any()) } answers { firstArg() }
                    every { passwordEncoder.encode(any()) } returns "encoded"

                    val result =
                            useCase.updateAdminUser(
                                    userId = adminId.toString(),
                                    email = "new@example.com",
                                    username = null,
                                    password = null
                            )

                    result.user.email.value shouldBe "new@example.com"
                }

                test("ROLE_ADMINユーザーの削除は成功する") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)
                    val adminId = UUID.randomUUID()
                    val adminUser = createTestUser(adminId, UserRole.ROLE_ADMIN)

                    every { userRepository.findById(any()) } returns adminUser
                    every { userRepository.deleteById(any()) } returns Unit

                    // 例外がスローされなければ成功
                    useCase.deleteAdminUser(adminId.toString())
                }
            }

            /**
             * Property 4: Admin一覧からのDeveloper除外 Validates: Requirements 7.1
             *
             * For any call to getAllAdminUsers(), the returned list should contain only users with
             * ROLE_ADMIN and never include users with ROLE_DEVELOPER.
             */
            context("Property 4: Admin一覧からのDeveloper除外") {
                test("getAllAdminUsersはROLE_DEVELOPERを含まない") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)

                    val users =
                            listOf(
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_USER),
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_ADMIN),
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_DEVELOPER),
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_ADMIN)
                            )

                    every { userRepository.findAll() } returns users

                    val result = useCase.getAllAdminUsers()

                    // ROLE_DEVELOPERが含まれていないことを確認
                    result.none { it.role == UserRole.ROLE_DEVELOPER } shouldBe true

                    // ROLE_ADMINのみが含まれていることを確認
                    result.all { it.role == UserRole.ROLE_ADMIN } shouldBe true

                    // 2人のADMINが返されることを確認
                    result.size shouldBe 2
                }

                test("getAllAdminUsersはROLE_USERも含まない") {
                    val useCase = createAdminUseCase(userRepository, passwordEncoder)

                    val users =
                            listOf(
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_USER),
                                    createTestUser(UUID.randomUUID(), UserRole.ROLE_ADMIN)
                            )

                    every { userRepository.findAll() } returns users

                    val result = useCase.getAllAdminUsers()

                    result.none { it.role == UserRole.ROLE_USER } shouldBe true
                    result.size shouldBe 1
                }
            }
        })

private fun createAdminUseCase(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
): AdminUseCase {
    return AdminUseCase(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            adminApiKey = "test-admin-key-not-default",
            devApiKey = "test-dev-key"
    )
}

private fun createTestUser(userId: UUID, role: UserRole): User {
    return User(
            userId = UserId(userId),
            username = Username("testuser_${userId.toString().take(8)}"),
            email = Email("test_${userId.toString().take(8)}@example.com"),
            passwordHash = PasswordHash("hashedpassword"),
            role = role
    )
}
