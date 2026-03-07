package com.king250.order.api.common.validator

import com.king250.order.api.common.annotation.NotNull
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.openapitools.jackson.nullable.JsonNullable

class NotNullValidator : ConstraintValidator<NotNull, JsonNullable<*>> {
    override fun isValid(value: JsonNullable<*>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null || !value.isPresent) return true
        return value.get() != null
    }
}