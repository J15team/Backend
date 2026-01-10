package com.j15.backend.domain.model.tag

import java.time.Instant

/** タグエンティティ（ドメイン層） 題材に付与できるラベルを表す */
data class Tag(
        val id: TagId,
        val name: TagName,
        val type: TagType = TagType.NORMAL,
        val createdAt: Instant = Instant.now()
) {
    companion object {
        /**
         * 新しいタグを作成する
         * @param id タグID
         * @param name タグ名（文字列）
         * @param type タグの種類（デフォルト: NORMAL）
         */
        fun create(id: TagId, name: String, type: TagType = TagType.NORMAL): Tag {
            return Tag(id = id, name = TagName(name), type = type, createdAt = Instant.now())
        }
    }

    /** 同じ名前を持つタグは等価とみなす */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
