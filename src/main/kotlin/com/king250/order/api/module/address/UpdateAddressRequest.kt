package com.king250.order.api.module.address

import com.king250.order.api.common.NotNullable
import jakarta.validation.constraints.Size
import org.openapitools.jackson.nullable.JsonNullable

data class UpdateAddressRequest(
    @field:NotNullable
    @field:Size(max = 20)
    var name: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Size(max = 11)
    var phone: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Size(max = 200)
    var address: JsonNullable<String> = JsonNullable.undefined(),
)
