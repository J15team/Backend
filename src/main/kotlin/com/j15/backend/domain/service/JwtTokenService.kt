package com.j15.backend.domain.service

import com.j15.backend.domain.model.auth.AccessToken
import com.j15.backend.domain.model.auth.RefreshToken
import com.j15.backend.domain.model.user.UserId

// JWTトークン生成・検証のドメインサービス
interface JwtTokenService {
    // アクセストークンの生成
    fun generateAccessToken(userId: UserId): AccessToken
    
    // リフレッシュトークンの生成
    fun generateRefreshToken(userId: UserId): RefreshToken
    
    // トークンからユーザーIDを取得
    fun getUserIdFromToken(token: String): UserId
    
    // トークンの検証
    fun validateToken(token: String): Boolean
}
