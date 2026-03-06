package com.king250.order.api.module.cart

import java.io.Serializable

data class UserItemId(
    val user: Long,
    val item: Long
) : Serializable
