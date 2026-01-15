package com.j15.backend.domain.model.assignment

/** 提出ステータス */
enum class SubmissionStatus {
    PENDING, // 判定待ち
    JUDGING, // 判定中
    COMPLETED // 判定完了
}
