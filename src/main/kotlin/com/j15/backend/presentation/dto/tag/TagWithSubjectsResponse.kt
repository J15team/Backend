package com.j15.backend.presentation.dto.tag

import com.j15.backend.domain.model.tag.Tag
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** タグ+紐づき題材IDレスポンス */
data class TagWithSubjectsResponse(
        val id: Long,
        val name: String,
        val displayName: String,
        val type: String,
        val createdAt: String,
        val subjectIds: List<Long>
) {
    companion object {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        fun from(tag: Tag, subjectIds: List<Long>): TagWithSubjectsResponse {
            return TagWithSubjectsResponse(
                    id = tag.id.value,
                    name = tag.name.value,
                    displayName = tag.name.toDisplayString(),
                    type = tag.type.name,
                    createdAt = tag.createdAt.atZone(ZoneId.systemDefault()).format(formatter),
                    subjectIds = subjectIds
            )
        }
    }
}
