package com.king250.order.api.module.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
class UserController(
    private val service: UserService,
    private val mapper: UserMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/users")
    fun getUsers(@Valid request: UserQueryRequest): ItemResponse<UserResponse> {
        val users = service.findAll(request)
        return users.toItem(mapper::toResponse)
    }

    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): UserResponse {
        val user = mapper.toEntity(request)
        return mapper.toResponse(service.save(user))
    }

    @PostMapping("/users/batch")
    suspend fun batchCreateUsers(@Valid @RequestBody request: UserBatchCreateRequest): ItemResponse<UserResponse> {
        val users = service.batchCreate(request)
        return users.toItem(mapper::toResponse)
    }

    @GetMapping("/users/nickname")
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

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        val user = service.findById(id)
        return mapper.toResponse(user)
    }

    @PatchMapping("/users/{id}")
    fun updateUserById(@PathVariable id: Long, @Valid @RequestBody request: UserUpdateRequest): UserResponse {
        val user = service.findById(id)
        mapper.updateEntity(request, user)
        return mapper.toResponse(service.save(user))
    }

    @DeleteMapping("/users/{id}")
    fun deleteUserById(@PathVariable id: Long) {
        service.deleteById(id)
    }
}
