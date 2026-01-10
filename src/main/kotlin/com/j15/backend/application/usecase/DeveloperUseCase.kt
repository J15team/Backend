package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.presentation.dto.response.EndpointInfo
import java.security.MessageDigest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/** 開発者専用操作のユースケース */
@Service
@Transactional
class DeveloperUseCase(
        private val userRepository: UserRepository,
        private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
        @Value("\${admin.dev-key:}") private val devApiKey: String
) {
    private val logger = LoggerFactory.getLogger(DeveloperUseCase::class.java)

    /** 開発者機能が有効かどうか */
    fun isDevModeEnabled(): Boolean {
        return devApiKey.isNotBlank()
    }

    /** 開発者キーを検証（定数時間比較） */
    fun validateDevKey(providedKey: String): Boolean {
        if (providedKey.length > 1000) {
            return false
        }

        if (devApiKey.isBlank()) {
            return false
        }

        return MessageDigest.isEqual(
                providedKey.toByteArray(Charsets.UTF_8),
                devApiKey.toByteArray(Charsets.UTF_8)
        )
    }

    /** 開発者キーを検証し、無効な場合は例外をスロー */
    fun requireValidDevKey(providedKey: String) {
        if (!validateDevKey(providedKey)) {
            throw SecurityException("Invalid dev API key")
        }
    }

    /** 全エンドポイント一覧を取得 */
    @Transactional(readOnly = true)
    fun getAllEndpoints(): List<EndpointInfo> {
        val handlerMethods = requestMappingHandlerMapping.handlerMethods
        return handlerMethods
                .map { (mapping, method) ->
                    val patterns =
                            mapping.pathPatternsCondition?.patterns
                                    ?: mapping.patternsCondition?.patterns ?: emptySet()
                    val path = patterns.firstOrNull()?.toString() ?: "unknown"
                    val httpMethods = mapping.methodsCondition.methods
                    val httpMethod = httpMethods.firstOrNull()?.name ?: "ANY"
                    val description = "${method.beanType.simpleName}.${method.method.name}()"
                    EndpointInfo(method = httpMethod, path = path, description = description)
                }
                .sortedWith(compareBy({ it.path }, { it.method }))
    }

    /**
     * ユーザーを削除（ROLE_DEVELOPER以外）
     * @return 削除成功した場合true
     * @throws SecurityException ROLE_DEVELOPERを削除しようとした場合
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    fun deleteUser(userId: String): Boolean {
        val userIdVo = UserId(java.util.UUID.fromString(userId))
        val user =
                userRepository.findById(userIdVo) ?: throw IllegalArgumentException("ユーザーが見つかりません")

        // ROLE_DEVELOPERは削除不可
        if (user.role == UserRole.ROLE_DEVELOPER) {
            throw SecurityException("開発者ユーザーは削除できません")
        }

        userRepository.deleteById(userIdVo)
        logger.info("Deleted user: $userId (role: ${user.role})")
        return true
    }
}
