package com.king250.order.api.module.user

import com.king250.order.api.common.NotNullable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern
import org.openapitools.jackson.nullable.JsonNullable

data class UserUpdateRequest(
    @field:Size(min = 1, max = 20)
    val name: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric")
    val qq: JsonNullable<String> = JsonNullable.undefined(),

    @field:Email
    val email: JsonNullable<String> = JsonNullable.undefined()
)