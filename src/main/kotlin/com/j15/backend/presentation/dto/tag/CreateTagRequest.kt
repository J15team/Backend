package com.j15.backend.presentation.dto.tag

/** タグ作成リクエスト */
data class CreateTagRequest(val name: String, val type: String? = "NORMAL")
