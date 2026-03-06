package com.king250.order.api.module.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern

data class UserCreateRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 20)
    var name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric")
    var qq: String,

    @field:Email
    var email: String? = null
)