package com.j15.backend.domain.model.section

// セクションID（値オブジェクト）
// 0~100の整数で進捗を管理
// 各整数値が1つの進捗ステップを表す
@JvmInline
value class SectionId(val value: Int) {
    init {
        require(value in 0..100) { "SectionIdは0~100の範囲である必要があります: $value" }
    }
}
