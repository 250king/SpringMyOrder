package com.king250.order.api.integration.logto

data class UserData(
    val id: String,

    val primaryEmail: String?,

    val username: String?,

    val name: String,

    val customData: CustomData,
)
