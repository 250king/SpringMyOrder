package com.king250.order.api.module.address

import com.king250.order.api.common.NotNullable
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.openapitools.jackson.nullable.JsonNullable

data class UpdateAddressRequest(
    @field:NotNullable
    @field:Size(max = 50)
    @field:Pattern(regexp = "^[a-zA-Z\\u4e00-\\u9fa5\\s]+$")
    var name: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Size(max = 20)
    @field:Pattern(regexp = "^1[3-9]\\d{9}$")
    var phone: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Size(max = 200)
    @field:Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\s\\-(),.#（）－]+$")
    var address: JsonNullable<String> = JsonNullable.undefined(),
)
