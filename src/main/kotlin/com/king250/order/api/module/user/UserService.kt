package com.king250.order.api.module.user

import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun findAll(request: UserQueryRequest): Page<User> {
        val spec = Specification<User> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            request.id?.let {
                predicates.add(cb.equal(root.get<Long>("id"), it))
            }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                val pattern = "%${kw}%"
                val nameLike = cb.like(root.get("name"), pattern)
                val qqLike = cb.like(root.get("qq"), pattern)
                predicates.add(cb.or(nameLike, qqLike))
            }
            cb.and(*predicates.toTypedArray())
        }
        return userRepository.findAll(spec, request.toPageable())
    }

    fun findByQq(qq: String): User? {
        return userRepository.findByQq(qq)
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }
}