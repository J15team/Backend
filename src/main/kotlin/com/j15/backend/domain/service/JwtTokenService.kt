package com.j15.backend.domain.service

import com.j15.backend.domain.model.auth.AccessToken
import com.j15.backend.domain.model.auth.RefreshToken
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.UserRole

// JWTトークン生成・検証のドメインサービス
interface JwtTokenService {
    // アクセストークンの生成
    fun generateAccessToken(userId: UserId, role: UserRole): AccessToken
    
    // リフレッシュトークンの生成
    fun generateRefreshToken(userId: UserId, role: UserRole): RefreshToken
    
    // トークンからユーザーIDを取得
    fun getUserIdFromToken(token: String): UserId
    
    // トークンからロールを取得
    fun getRoleFromToken(token: String): UserRole
    
    // トークンの検証
    fun validateToken(token: String): Boolean
}
