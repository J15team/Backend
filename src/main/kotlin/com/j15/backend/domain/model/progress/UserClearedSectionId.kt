package com.j15.backend.domain.model.progress

// 完了記録ID（値オブジェクト）
@JvmInline
value class UserClearedSectionId(val value: Int) {
    init {
        require(value > 0) { "UserClearedSectionIdは正の整数である必要があります" }
    }
}
