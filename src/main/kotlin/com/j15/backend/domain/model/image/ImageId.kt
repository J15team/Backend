package com.j15.backend.domain.model.image

/**
 * 画像ID（値オブジェクト）
 * データベースで自動生成される連番ID
 */
@JvmInline
value class ImageId(val value: Long) {
    init {
        require(value > 0) { "ImageIdは正の整数である必要があります: $value" }
    }
}
