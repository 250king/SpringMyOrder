package com.king250.order.api.module.user

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class UserBatchCreateRequest(
    @field:NotEmpty
    @field:Valid
    val users: List<@Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric") String>
)
