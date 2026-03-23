package com.king250.order.api.module.group

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.tables.records.GroupRecord
import com.king250.order.jooq.tables.references.GROUP
import com.king250.order.jooq.tables.references.GROUP_USER
import com.king250.order.jooq.tables.references.USER
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val dsl: DSLContext,
    private val auth: AuthService
) {
    fun findAll(request: QueryGroupRequest): Page<GroupRecord> {
        val pageable = request.toPageable()
        val sortMap = mapOf(
            "id" to GROUP.ID,
            "name" to GROUP.NAME,
            "status" to GROUP.STATUS,
            "deadline" to GROUP.DEADLINE,
            "created_at" to GROUP.CREATED_AT,
            "updated_at" to GROUP.UPDATED_AT
        )
        val conditions = mutableListOf<Condition>().apply {
            request.id?.let { add(GROUP.ID.eq(it)) }
            request.status?.let { add(GROUP.STATUS.eq(it)) }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                add(GROUP.NAME.containsIgnoreCase(kw).or(GROUP.QQ.containsIgnoreCase(kw)))
            }
            if (auth.isAdmin()) {
                request.userId?.let { uid ->
                    add(GROUP.ID.`in`(
                        dsl.select(GROUP_USER.GROUP_ID)
                            .from(GROUP_USER)
                            .where(GROUP_USER.USER_ID.eq(uid))
                    ))
                }
            } else {
                add(GROUP.ID.`in`(
                    dsl.select(GROUP_USER.GROUP_ID)
                        .from(GROUP_USER)
                        .where(GROUP_USER.USER_ID.eq(auth.getUid()))
                ))
            }
        }
        val total = dsl.fetchCount(GROUP, conditions)
        val records = dsl.selectFrom(GROUP)
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): GroupRecord {
        return dsl.selectFrom(GROUP)
            .where(GROUP.ID.eq(id))
            .fetchSingle()
    }

    @Transactional
    fun save(group: GroupRecord, memberIds: List<Long> = emptyList()): GroupRecord {
        if (group.id == null) {
            val inserted = dsl.insertInto(GROUP)
                .set(group)
                .returning()
                .fetchOne()!!
            val list = memberIds.toMutableSet()
            addMembers(inserted.id!!, list)
            return inserted
        } else {
            dsl.attach(group)
            group.store()
            return group
        }
    }

    fun getMembers(request: QueryMemberRequest, groupId: Long): Page<Record> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>().apply {
            add(GROUP_USER.GROUP_ID.eq(groupId))
            request.id?.let { add(GROUP_USER.USER_ID.eq(it)) }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                add(USER.NAME.containsIgnoreCase(kw).or(USER.QQ.containsIgnoreCase(kw)))
            }
        }
        val sortMap = mapOf(
            "id" to USER.ID,
            "user.id" to USER.ID,
            "user.name" to USER.NAME,
            "user.credit_score" to USER.CREDIT_SCORE,
            "created_at" to GROUP_USER.CREATED_AT
        )
        val total = dsl.fetchCount(
            GROUP_USER.join(USER).on(USER.ID.eq(GROUP_USER.USER_ID)),
            conditions
        )
        val records = dsl.select(*USER.fields(), *GROUP_USER.fields())
            .from(GROUP_USER)
            .join(USER).on(USER.ID.eq(GROUP_USER.USER_ID))
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize).offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    @Transactional
    fun addMembers(groupId: Long, userIds: Set<Long>): Int {
        val inserts = userIds.map { userId ->
            dsl.insertInto(GROUP_USER)
                .set(GROUP_USER.GROUP_ID, groupId)
                .set(GROUP_USER.USER_ID, userId)
                .onDuplicateKeyIgnore()
        }
        return dsl.batch(inserts).execute().filter { it == 1 }.size
    }

    /*
    @Transactional
    fun removeMember(groupId: Long, userId: Long) {
        val user = dsl.selectFrom(GROUP_USER)
            .where(GROUP_USER.USER_ID.eq(userId))
            .and(GROUP_USER.GROUP_ID.eq(groupId))
            .fetchSingle()
        if (user.role == GroupRole.OWNER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot be removed from the group.")
        }
        //TODO: 需要添加是否有活跃中的订单的检查
        dsl.deleteFrom(GROUP_USER)
            .where(GROUP_USER.USER_ID.eq(userId))
            .and(GROUP_USER.GROUP_ID.eq(groupId))
            .execute()
        dsl.delete(CART)
            .using(ITEM)
            .where(CART.ITEM_ID.eq(ITEM.ID))
            .and(CART.USER_ID.eq(userId))
            .and(ITEM.GROUP_ID.eq(groupId))
            .execute()
    }
    */

    /*
    fun deleteById(id: Long) {
        dsl.deleteFrom(GROUP)
            .where(GROUP.ID.eq(id))
            .execute()
    }
    */
}
