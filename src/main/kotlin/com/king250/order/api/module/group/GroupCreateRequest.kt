package com.king250.order.api.module.group

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.Instant

data class GroupCreateRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric")
    val qq: String,

    @field:Future
    val deadline: Instant?
)