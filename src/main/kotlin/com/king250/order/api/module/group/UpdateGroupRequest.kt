package com.king250.order.api.module.group

import com.king250.order.api.common.NotNullable
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.openapitools.jackson.nullable.JsonNullable
import java.time.Instant

data class UpdateGroupRequest(
    @field:NotNullable
    @field:Size(min = 1, max = 50)
    val name: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Pattern(regexp = "^\\d+$", message = "QQ must be numeric")
    val qq: JsonNullable<String> = JsonNullable.undefined(),

    @field:NotNullable
    @field:Future
    val deadline: JsonNullable<Instant> = JsonNullable.undefined(),
)