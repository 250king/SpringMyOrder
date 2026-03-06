package com.king250.order.api.module.user

import com.king250.order.api.common.response.ItemResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {
    @GetMapping("/users")
    fun getUsers(request: UserQueryRequest) {
        val users = userService.findAll(request)
    }
}