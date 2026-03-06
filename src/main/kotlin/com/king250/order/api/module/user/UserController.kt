package com.king250.order.api.module.user

import com.king250.order.api.common.response.ItemResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
    private val userMapper: UserMapper
) {
    @GetMapping("/users")
    fun getUsers(@Valid request: UserQueryRequest): ItemResponse<UserResponse> {
        val users = userService.findAll(request)
        return ItemResponse(userMapper.toResponseList(users.content), users.totalElements)
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): UserResponse {
        val user = userService.findById(id)
        return userMapper.toResponse(user)
    }

    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): UserResponse {
        val user = userMapper.toEntity(request)
        return userMapper.toResponse(userService.save(user))
    }
}