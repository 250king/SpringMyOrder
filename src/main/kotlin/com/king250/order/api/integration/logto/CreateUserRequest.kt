package com.king250.order.api.integration.logto

data class CreateUserRequest(
    val primaryEmail: String,

    val name: String,

    val avatar: String,

    val customData: CustomData
)
