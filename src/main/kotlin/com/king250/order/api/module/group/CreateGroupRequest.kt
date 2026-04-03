package com.king250.order.api.module.group

import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import java.time.Instant

data class CreateGroupRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val name: String,

    @field:NotBlank
    @field:Pattern(regexp = "^\\d+$", message = "QQ must be numeric")
    val qq: String,

    @field:Future
    val deadline: Instant?,

    @field:NotNull
    val ownerId: Long,

    @field:URL
    val image: String?,

    @field:Valid
    @field:NotEmpty
    val users: List<@NotNull Long>
)