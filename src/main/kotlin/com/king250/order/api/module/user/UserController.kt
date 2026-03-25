package com.king250.order.api.module.user

import com.king250.order.api.common.ItemResponse
import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Suppress("UastIncorrectHttpHeaderInspection")
@RestController
@Validated
class UserController(
    private val service: UserService,
    private val mapper: UserMapper,
    private val auth: AuthService
) {
    @GetMapping("/admin/users")
    fun findAll(@Valid @ParameterObject request: QueryUserRequest): ItemResponse<UserResponse> {
        val users = service.findAll(request)
        return users.toItem(mapper::toResponse)
    }

    @GetMapping("/admin/users/{userId}")
    fun findById(@PathVariable userId: Long): UserResponse {
        val user = service.findById(userId)
        return mapper.toResponse(user)
    }

    @PostMapping("/webhook/user")
    fun webhook(@RequestBody body: String, @RequestHeader("logto-signature-sha-256") signature: String) {
        service.webhook(body, signature)
    }

    @GetMapping("/me")
    fun getMe(): MeResponse {
        val user = service.findById(auth.getUid())
        return mapper.toMeResponse(user, auth.isAdmin())
    }
}
