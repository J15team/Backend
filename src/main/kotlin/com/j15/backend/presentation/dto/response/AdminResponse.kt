package com.j15.backend.presentation.dto.response

data class AdminUserCreateResponse(
    val userId: String,
    val email: String,
    val username: String,
    val role: String,
    val message: String = "Admin user created successfully"
)

data class AdminUserResponse(
    val userId: String,
    val email: String,
    val username: String,
    val role: String
)

data class AdminUserUpdateResponse(
    val userId: String,
    val email: String,
    val username: String,
    val role: String,
    val message: String = "Admin user updated successfully"
)

data class AdminUsersListResponse(
    val admins: List<AdminUserResponse>
)
