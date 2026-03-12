package com.king250.order.api.module.group

import com.king250.order.api.module.user.UserResponse
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.enums.Role
import com.king250.order.jooq.tables.records.GroupRecord
import com.king250.order.jooq.tables.references.CART
import com.king250.order.jooq.tables.references.GROUP
import com.king250.order.jooq.tables.references.GROUP_USER
import com.king250.order.jooq.tables.references.ITEM
import com.king250.order.jooq.tables.references.USER
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GroupService(
    private val dsl: DSLContext
) {
    fun findAll(request: GroupQueryRequest): Page<GroupRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        val sortMap = mapOf(
            "id" to GROUP.ID,
            "name" to GROUP.NAME,
            "status" to GROUP.STATUS,
            "deadline" to GROUP.DEADLINE,
            "created_at" to GROUP.CREATED_AT,
            "updated_at" to GROUP.UPDATED_AT
        )
        request.id?.let {
            conditions.add(GROUP.ID.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            conditions.add(GROUP.NAME.containsIgnoreCase(kw).or(GROUP.QQ.containsIgnoreCase(kw)))
        }
        request.ownerId?.let {
            conditions.add(GROUP.OWNER_ID.eq(it))
        }
        request.status?.let {
            conditions.add(GROUP.STATUS.eq(it))
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
            val list = memberIds.toMutableSet().apply { add(group.ownerId!!) }
            addMembersToGroup(inserted.id!!, list, group.ownerId)
            return inserted
        } else {
            dsl.attach(group)
            group.store()
            return group
        }
    }

    fun getMembers(request: MemberQueryRequest, groupId: Long): Page<MemberResponse> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        val sortMap = mapOf(
            "id" to USER.ID,
            "user.id" to USER.ID,
            "user.name" to USER.NAME,
            "user.credit_score" to USER.CREDIT_SCORE,
            "role" to GROUP_USER.ROLE,
            "created_at" to GROUP_USER.CREATED_AT
        )
        request.id?.let {
            conditions.add(GROUP_USER.USER_ID.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            conditions.add(USER.NAME.containsIgnoreCase(kw).or(USER.QQ.containsIgnoreCase(kw)))
        }
        request.role?.let {
            conditions.add(GROUP_USER.ROLE.eq(it))
        }
        conditions.add(GROUP_USER.GROUP_ID.eq(groupId))
        val total = dsl.fetchCount(
            GROUP_USER.join(USER).on(USER.ID.eq(GROUP_USER.USER_ID)),
            conditions
        )
        val records = dsl.select(*USER.fields(), *GROUP_USER.fields())
            .from(GROUP_USER)
            .join(USER)
            .on(USER.ID.eq(GROUP_USER.USER_ID))
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { r ->
                MemberResponse(
                    user = r.into(USER).into(UserResponse::class.java),
                    role = r.get(GROUP_USER.ROLE)!!,
                    createdAt = r.get(GROUP_USER.CREATED_AT)!!
                )
            }
         return PageImpl(records, pageable, total.toLong())
    }

    @Transactional
    fun addMembersToGroup(groupId: Long, userIds: Set<Long>, ownerId: Long? = null) {
        if (userIds.isEmpty()) return
        val inserts = userIds.map { userId ->
            val actualRole = if (userId == ownerId) Role.OWNER else Role.MEMBER
            dsl.insertInto(GROUP_USER)
                .set(GROUP_USER.GROUP_ID, groupId)
                .set(GROUP_USER.USER_ID, userId)
                .set(GROUP_USER.ROLE, actualRole)
                .onDuplicateKeyIgnore()
        }
        dsl.batch(inserts).execute()
    }

    @Transactional
    fun removeMemberFromGroup(groupId: Long, userId: Long) {
        val user = dsl.selectFrom(GROUP_USER)
            .where(GROUP_USER.USER_ID.eq(userId))
            .and(GROUP_USER.GROUP_ID.eq(groupId))
            .fetchSingle()
        if (user.role == Role.OWNER) {
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

    @Transactional
    fun changeMemberRole(groupId: Long, userId: Long, role: Role) {
        if (role == Role.OWNER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign owner role to a member.")
        }
        val user = dsl.selectFrom(GROUP_USER)
            .where(GROUP_USER.USER_ID.eq(groupId))
            .and(GROUP_USER.GROUP_ID.eq(userId))
            .fetchSingle()
        if (user.role == Role.OWNER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner's role cannot be changed.")
        }
        dsl.update(GROUP_USER)
            .set(GROUP_USER.ROLE, role)
            .where(GROUP_USER.USER_ID.eq(groupId))
            .and(GROUP_USER.GROUP_ID.eq(userId))
            .execute()
    }

    @Transactional
    fun transferOwnership(groupId: Long, ownerId: Long) {
        val owner = dsl.selectFrom(GROUP_USER)
            .where(GROUP_USER.GROUP_ID.eq(groupId))
            .and(GROUP_USER.USER_ID.eq(ownerId))
            .fetchSingle()
        if (owner.role == Role.OWNER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "New owner is already the current owner.")
        }
        dsl.update(GROUP_USER)
            .set(GROUP_USER.ROLE, Role.MEMBER)
            .where(GROUP_USER.GROUP_ID.eq(groupId))
            .and(GROUP_USER.ROLE.eq(Role.OWNER))
            .execute()
        dsl.update(GROUP_USER)
            .set(GROUP_USER.ROLE, Role.OWNER)
            .where(GROUP_USER.GROUP_ID.eq(groupId))
            .and(GROUP_USER.USER_ID.eq(ownerId))
            .execute()
        dsl.update(GROUP)
            .set(GROUP.OWNER_ID, ownerId)
            .where(GROUP.ID.eq(groupId))
            .execute()
    }

    fun deleteById(id: Long) {
        dsl.deleteFrom(GROUP)
            .where(GROUP.ID.eq(id))
            .execute()
    }
}
