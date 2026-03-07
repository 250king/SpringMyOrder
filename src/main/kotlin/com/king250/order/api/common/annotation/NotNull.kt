package com.king250.order.api.common.annotation

import com.king250.order.api.common.validator.NotNullValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotNullValidator::class])
annotation class NotNull(
    val message: String = "This field cannot be null",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
