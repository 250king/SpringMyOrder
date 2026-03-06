package com.king250.order.api.module.user

import com.king250.order.api.module.user.QUser.user
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findAll(request: UserQueryRequest): Page<User> {
        val builder = BooleanBuilder()
        request.id?.let {
            builder.and(user.id.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            builder.and(user.name.containsIgnoreCase(kw).or(user.qq.containsIgnoreCase(kw)))
        }
        return userRepository.findAll(builder, request.toPageable())
    }

    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "User with id $id not found")
        }
    }

    fun findByQq(qq: String): User? {
        return userRepository.findByQq(qq)
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }
}