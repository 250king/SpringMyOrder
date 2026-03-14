package com.king250.order.api.module.user

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class BatchCreateUserRequest(
    @field:NotEmpty
    @field:Valid
    val users: List<@NotBlank @Size(max = 40) @Pattern(regexp = "^\\d+$", message = "QQ must be numeric") String>
)
