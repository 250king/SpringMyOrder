package com.king250.order.api.module.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
class UserController(
    private val service: UserService,
    private val mapper: UserMapper,
    private val objectMapper: ObjectMapper,
    private val auth: AuthService
) {
    @GetMapping("/users")
    @PreAuthorize("@auth.isSuperAdmin()")
    fun findAll(@Valid request: QueryUserRequest): ItemResponse<UserResponse> {
        val users = service.findAll(request)
        return users.toItem(mapper::toResponse)
    }

    @PostMapping("/users")
    @PreAuthorize("@auth.isSuperAdmin()")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): UserResponse {
        val user = mapper.toEntity(request)
        return mapper.toResponse(service.save(user))
    }

    @PostMapping("/users/batch")
    @PreAuthorize("@auth.isSuperAdmin()")
    suspend fun batchCreate(@Valid @RequestBody request: BatchCreateUserRequest): ObjectNode {
        val result = service.batchCreate(request)
        return objectMapper.createObjectNode().apply {
            put("total", result)
        }
    }

    @GetMapping("/users/nickname")
    @PreAuthorize("@auth.isSuperAdmin()")
    suspend fun getNickname(
        @Pattern(regexp = "^[0-9]*$", message = "QQ must be numeric")
        @RequestParam
        qq: String
    ): ObjectNode {
        val nickname = service.getNickname(qq)
        return objectMapper.createObjectNode().apply {
            put("name", nickname)
        }
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("@auth.isSelf(#userId)")
    fun findById(@PathVariable userId: Long): UserResponse {
        val user = service.findById(userId)
        return mapper.toResponse(user)
    }

    @PatchMapping("/users/{userId}")
    @PreAuthorize("@auth.isSelf(#userId)")
    fun update(@PathVariable userId: Long, @Valid @RequestBody request: UpdateUserRequest): UserResponse {
        val user = service.findById(userId)
        mapper.updateEntity(request, user)
        return mapper.toResponse(service.save(user))
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@auth.isSuperAdmin()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable userId: Long) {
        service.deleteById(userId)
    }

    @GetMapping("/me")
    fun getMe(): UserResponse {
        val user = service.findById(auth.getUid())
        return mapper.toResponse(user)
    }

    @PatchMapping("/me")
    fun updateMe(@Valid @RequestBody request: UpdateUserRequest): UserResponse {
        val user = service.findById(auth.getUid())
        mapper.updateEntity(request, user)
        return mapper.toResponse(service.save(user))
    }
}
