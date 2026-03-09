package com.king250.order.api.module.address

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class AddressCreateRequest(
    @field:NotNull
    var userId: Long,

    @field:NotBlank
    @field:Size(max = 20)
    var name: String,

    @field:NotBlank
    @field:Size(max = 11)
    var phone: String,

    @field:NotBlank
    @field:Size(max = 200)
    var address: String
)
