package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.model.tag.TagType
import com.j15.backend.infrastructure.entity.TagJpaEntity
import java.time.ZoneId
import org.springframework.stereotype.Component

@Component
class TagConverter {

    fun toDomain(entity: TagJpaEntity): Tag {
        return Tag(
                id = TagId(entity.tagId),
                name = TagName(entity.name),
                type = TagType.valueOf(entity.type),
                createdAt = entity.createdAt.atZone(ZoneId.systemDefault()).toInstant()
        )
    }

    fun toEntity(domain: Tag): TagJpaEntity {
        return TagJpaEntity(
                tagId = domain.id.value,
                name = domain.name.value,
                type = domain.type.name,
                createdAt = domain.createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    /** 新規作成用（IDなし） */
    fun toNewEntity(domain: Tag): TagJpaEntity {
        return TagJpaEntity(
                tagId = 0L,
                name = domain.name.value,
                type = domain.type.name,
                createdAt = domain.createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }
}
