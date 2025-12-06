package com.j15.backend.presentation.dto.subject

/** 題材更新リクエスト */
data class UpdateSubjectRequest(val title: String, val description: String?, val maxSections: Int)
