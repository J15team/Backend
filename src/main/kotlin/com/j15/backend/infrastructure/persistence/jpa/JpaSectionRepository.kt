package com.j15.backend.infrastructure.persistence.jpa

import com.j15.backend.infrastructure.persistence.entity.SectionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JpaSectionRepository : JpaRepository<SectionEntity, Int>
