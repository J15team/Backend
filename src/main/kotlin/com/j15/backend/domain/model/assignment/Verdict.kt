package com.j15.backend.domain.model.assignment

/** 判定結果 */
enum class Verdict {
    AC, // Accepted（正解）
    WA, // Wrong Answer（不正解）
    TLE, // Time Limit Exceeded（時間超過）
    MLE, // Memory Limit Exceeded（メモリ超過）
    RE, // Runtime Error（実行時エラー）
    CE // Compilation Error（コンパイルエラー）
}
