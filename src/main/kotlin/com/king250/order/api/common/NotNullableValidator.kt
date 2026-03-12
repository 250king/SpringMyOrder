package com.king250.order.api.common

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.openapitools.jackson.nullable.JsonNullable

class NotNullableValidator : ConstraintValidator<NotNullable, Any?> {
    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        if (value is JsonNullable<*>) {
            return !(value.isPresent && value.get() == null)
        }
        if (value == null) {
            return false
        }
        return true
    }
}