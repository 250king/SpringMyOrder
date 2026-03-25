package com.king250.order.api.module.user

import com.king250.order.api.integration.logto.LogtoService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.USER
import org.apache.commons.codec.digest.DigestUtils
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val dsl: DSLContext,
    private val logto: LogtoService
) {
    fun findAll(request: QueryUserRequest): Page<UserRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        val sortMap = mapOf(
            "id" to USER.ID,
            "name" to USER.NAME,
            "email" to USER.EMAIL,
            "creditScore" to USER.CREDIT_SCORE,
            "createdAt" to USER.CREATED_AT,
            "updatedAt" to USER.UPDATED_AT,
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
            .and(USER.DELETED_AT.isNull)
            .fetchSingle()
    }

    fun webhook(body: String, signature: String) {
        if (!logto.checkSignature(body, signature)) {
            throw AuthorizationDeniedException("Invalid signature")
        }
        val data = logto.parseData(body)
        if (data.event == "User.Data.Updated" || data.event == "User.Created") {
            data.data?.let {
                if (it.customData.qq == null) {
                    return
                }
                val user = dsl.selectFrom(USER)
                    .where(USER.REFERENCE_ID.eq(it.id))
                    .fetchOne() ?: UserRecord()
                user.name = it.name
                user.qq = it.customData.qq
                user.email = if (it.primaryEmail == "${it.customData.qq}@qq.com") {
                    null
                } else {
                    it.primaryEmail
                }
                user.store()
            }
        } else if (data.event == "User.Deleted") {
            val user = dsl.selectFrom(USER)
                .where(USER.REFERENCE_ID.eq(data.params.userId))
                .fetchOne() ?: return
            try {
                user.delete()
            } catch (_: Exception) {
                user.name = "已注销_${user.id}"
                user.qq = DigestUtils.md5Hex(user.qq)
                user.email = null
                user.referenceId = null
                user.deletedAt = Instant.now()
                user.store()
            }
        }
    }
}