package com.j15.backend.domain.model.tag

/** タグの種類を表すEnum 将来のプレミアムタグ拡張に対応 */
enum class TagType {
    /** 通常タグ（管理者が自由に作成） */
    NORMAL,

    /** プレミアムタグ（将来拡張用：企業名等） */
    PREMIUM
}
