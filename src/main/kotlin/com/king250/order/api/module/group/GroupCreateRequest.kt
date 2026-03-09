package com.king250.order.api.module.group

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class GroupCreateRequest(
    @field:NotEmpty
    val name: String,

    @field:NotEmpty
    @field:Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric")
    val qq: String
)