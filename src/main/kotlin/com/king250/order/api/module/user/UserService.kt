package com.king250.order.api.module.user

import com.king250.order.api.common.util.toJooq
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.USER
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL.castNull
import org.jooq.impl.DSL.concat
import org.jooq.impl.DSL.left
import org.jooq.impl.DSL.md5
import org.jooq.impl.DSL.currentInstant
import org.jooq.impl.DSL.`val`
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val dsl: DSLContext,
) {
    fun findAll(request: UserQueryRequest): PageImpl<UserRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        conditions.add(USER.DELETED_AT.isNull)
        request.id?.let {
            conditions.add(USER.ID.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            conditions.add(USER.NAME.containsIgnoreCase(kw).or(USER.QQ.containsIgnoreCase(kw)))
        }
        val total = dsl.fetchCount(USER, conditions)
        val records = dsl.selectFrom(USER)
            .where(conditions)
            .orderBy(pageable.sort.toJooq(USER))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): UserRecord {
        return dsl.selectFrom(USER)
            .where(USER.ID.eq(id))
            .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun findByQq(qq: String): UserRecord? {
        return dsl.selectFrom(USER)
            .where(USER.QQ.eq(qq))
            .fetchOne()
    }

    @Transactional
    fun save(user: UserRecord): UserRecord {
        try {
            if (user.id == null) {
                return dsl.insertInto(USER)
                    .set(user)
                    .returning()
                    .fetchOne()!!
            } else {
                dsl.attach(user)
                user.store()
                return user
            }
        } catch (_: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT)
        }
    }

    @Transactional
    fun deleteById(id: Long) {
        if (dsl.fetchExists(USER.where(USER.ID.eq(id).and(USER.DELETED_AT.isNull)))) {
            dsl.update(USER)
                .set(USER.NAME, concat(`val`("已注销_"), left(md5(USER.NAME), 8)))
                .set(USER.QQ, md5(USER.QQ))
                .set(USER.EMAIL, castNull(USER.EMAIL))
                .set(USER.DELETED_AT, currentInstant())
                .where(USER.ID.eq(id))
                .execute()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id $id not found")
        }
    }
}