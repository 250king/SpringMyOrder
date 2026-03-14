package com.king250.order.api.module.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Pattern

data class CreateUserRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val name: String,

    @field:NotBlank
    @field:Size(max = 40)
    @field:Pattern(regexp = "^\\d+$", message = "QQ must be numeric")
    val qq: String,

    @field:Size(max = 255)
    @field:Email
    val email: String?
)