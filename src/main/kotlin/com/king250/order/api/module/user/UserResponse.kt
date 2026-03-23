package com.king250.order.api.module.user

data class UserResponse(
    val id: Long,

    val name: String,

    val qq: String,

    val email: String?,

    val creditScore: Int,

    val createdAt: String,

    val updatedAt: String,

    val isAdmin: Boolean?,
)