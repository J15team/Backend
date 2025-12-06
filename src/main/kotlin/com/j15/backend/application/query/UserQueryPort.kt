package com.j15.backend.application.query

import com.j15.backend.domain.model.user.User

// リードモデル用のクエリポート（必要に応じて投影・検索を担う）
interface UserQueryPort {
    fun findAll(): List<User>
}
