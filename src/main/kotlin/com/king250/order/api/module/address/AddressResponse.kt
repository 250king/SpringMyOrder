package com.king250.order.api.module.address

data class AddressResponse(
    val id: Long,

    val name: String,

    val phone: String,

    val address: String
)