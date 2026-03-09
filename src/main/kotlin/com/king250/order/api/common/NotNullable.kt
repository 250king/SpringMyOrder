package com.king250.order.api.common

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotNullableValidator::class])
annotation class NotNullable(
    val message: String = "This field cannot be null",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
