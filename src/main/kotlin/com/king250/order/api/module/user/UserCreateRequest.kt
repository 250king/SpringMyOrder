package com.king250.order.api.module.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern

data class UserCreateRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d+$", message = "QQ must be numeric")
    val qq: String,

    @field:Email
    val email: String?
)