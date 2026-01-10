package com.j15.backend.presentation.dto.response

/** エンドポイント情報 */
data class EndpointInfo(val method: String, val path: String, val description: String)

/** 開発者確認レスポンス */
data class DeveloperVerifyResponse(val isDeveloper: Boolean)

/** エンドポイント一覧レスポンス */
data class EndpointsListResponse(val endpoints: List<EndpointInfo>, val count: Int)
