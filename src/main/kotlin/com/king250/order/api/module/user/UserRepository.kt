package com.king250.order.api.module.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface UserRepository : JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {
    fun findByQq(qq: String): User?
}