package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.TagJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaTagRepository : JpaRepository<TagJpaEntity, Long> {
    fun findByName(name: String): TagJpaEntity?
    fun findByType(type: String): List<TagJpaEntity>
    fun findByNameContainingIgnoreCase(query: String): List<TagJpaEntity>
    fun findAllByOrderByNameAsc(): List<TagJpaEntity>
    fun existsByName(name: String): Boolean
}
