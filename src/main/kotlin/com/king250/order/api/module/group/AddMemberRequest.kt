package com.king250.order.api.module.group

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class AddMemberRequest(
    @field:Valid
    @field:NotEmpty
    val users: List<@NotNull @Positive Long>,
)