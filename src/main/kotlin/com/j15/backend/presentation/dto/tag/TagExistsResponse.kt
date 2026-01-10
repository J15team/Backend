package com.j15.backend.presentation.dto.tag

/** タグ存在確認レスポンス */
data class TagExistsResponse(val exists: Boolean, val tag: TagResponse?)
