package com.j15.backend.domain.exception

/** 課題題材が見つからない例外 */
class AssignmentNotFoundException(message: String) : RuntimeException(message)

/** セクションが見つからない例外 */
class SectionNotFoundException(message: String) : RuntimeException(message)

/** 課題なしセクションへの提出例外 */
class NoAssignmentException(message: String) : RuntimeException(message)
