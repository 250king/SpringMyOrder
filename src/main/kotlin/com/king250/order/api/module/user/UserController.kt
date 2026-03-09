package com.king250.order.api.module.user

import com.king250.order.api.common.ItemResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val service: UserService,
    private val mapper: UserMapper
) {
    @GetMapping("/users")
    fun getUsers(@Valid request: UserQueryRequest): ItemResponse<UserResponse> {
        val users = service.findAll(request)
        return ItemResponse(mapper.toResponseList(users.content), users.totalElements)
    }

    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): UserResponse {
        val user = mapper.toEntity(request)
        return mapper.toResponse(service.save(user))
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
