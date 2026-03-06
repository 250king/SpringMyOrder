package com.king250.order.api.module.user

import com.fasterxml.jackson.annotation.JsonProperty

data class UserResponse(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("qq")
    val qq: String,

    @JsonProperty("email")
    val email: String?,

    @JsonProperty("credit_score")
    val creditScore: Int,

    @JsonProperty("created_at")
    val createdAt: String,

    @JsonProperty("updated_at")
    val updatedAt: String
)