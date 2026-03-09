package com.king250.order.api.common

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.openapitools.jackson.nullable.JsonNullable

class NotNullValidator : ConstraintValidator<NotNullable, JsonNullable<*>> {
    override fun isValid(value: JsonNullable<*>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null || !value.isPresent) return true
        return value.get() != null
    }
}