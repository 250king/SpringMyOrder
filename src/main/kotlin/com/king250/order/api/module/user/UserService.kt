package com.king250.order.api.module.user

import com.king250.order.api.integration.napcat.NapcatService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.USER
import kotlinx.coroutines.*
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.exception.NoDataFoundException
import org.jooq.impl.DSL.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val dsl: DSLContext,
    private val napcat: NapcatService,
) {
    fun findAll(request: UserQueryRequest): Page<UserRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        val sortMap = mapOf(
            "id" to USER.ID,
            "name" to USER.NAME,
            "email" to USER.EMAIL,
            "credit_score" to USER.CREDIT_SCORE,
            "created_at" to USER.CREATED_AT
        )
        request.id?.let {
            conditions.add(USER.ID.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            conditions.add(USER.NAME.containsIgnoreCase(kw).or(USER.QQ.containsIgnoreCase(kw)))
        }
        conditions.add(USER.DELETED_AT.isNull)
        val total = dsl.fetchCount(USER, conditions)
        val records = dsl.selectFrom(USER)
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): UserRecord {
        return dsl.selectFrom(USER)
            .where(USER.ID.eq(id))
            .fetchSingle()
    }

    suspend fun batchCreate(request: UserBatchCreateRequest): Page<UserRecord> {
        val list = coroutineScope {
            request.users.toSet().map { qq ->
                async {
                    qq to getNickname(qq)
                }
            }.awaitAll().toMap()
        }
        return withContext(Dispatchers.IO) {
            val query = dsl.insertInto(USER, USER.NAME, USER.QQ).apply {
                list.forEach {
                    values(it.value, it.key)
                }
            }
            val records = query.onDuplicateKeyUpdate()
                .set(USER.NAME, excluded(USER.NAME))
                .returning()
                .fetch()
            PageImpl(records, Pageable.unpaged(), records.size.toLong())
        }
    }

    suspend fun getNickname(userId: String): String {
        val name = napcat.getUserInfo(userId).data?.nick
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The QQ user does not exist")
        if (name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "The QQ user does not exist")
        }
        return name
    }

    @Transactional
    fun save(user: UserRecord): UserRecord {
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
    }

    @Transactional
    fun deleteById(id: Long) {
        if (dsl.fetchExists(USER.where(USER.ID.eq(id).and(USER.DELETED_AT.isNull)))) {
            try {
                dsl.deleteFrom(USER)
                    .where(USER.ID.eq(id))
                    .execute()
            } catch (_: Exception) {
                dsl.update(USER)
                    .set(USER.NAME, concat(`val`("已注销_"), left(md5(USER.NAME), 8)))
                    .set(USER.QQ, md5(USER.QQ))
                    .set(USER.EMAIL, castNull(USER.EMAIL))
                    .set(USER.DELETED_AT, currentInstant())
                    .where(USER.ID.eq(id))
                    .execute()
            }
        } else {
            throw NoDataFoundException()
        }
    }
}