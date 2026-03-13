package com.king250.order.api.module.address

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.tables.records.AddressRecord
import com.king250.order.jooq.tables.references.ADDRESS
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.exception.NoDataFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddressService(
    private val dsl: DSLContext,
    private val auth: AuthService
) {
    fun findAll(request: QueryAddressRequest): Page<AddressRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>().apply {
            if (!auth.isSuperAdmin()) {
                add(ADDRESS.USER_ID.eq(auth.getUid()))
            } else if (request.userId != null) {
                add(ADDRESS.USER_ID.eq(request.userId))
            }
            request.id?.let {
                add(ADDRESS.ID.eq(it))
            }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                add(ADDRESS.NAME.containsIgnoreCase(kw)
                    .or(ADDRESS.PHONE.containsIgnoreCase(kw))
                    .or(ADDRESS.ADDRESS_.containsIgnoreCase(kw))
                )
            }
        }
        val sortMap = mapOf(
            "id" to ADDRESS.ID,
            "user_id" to ADDRESS.USER_ID,
            "name" to ADDRESS.NAME,
            "phone" to ADDRESS.PHONE,
            "address" to ADDRESS.ADDRESS_
        )
        val total = dsl.fetchCount(ADDRESS, conditions)
        val records = dsl.selectFrom(ADDRESS)
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): AddressRecord {
        return dsl.selectFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
            .let {
                if (auth.isSuperAdmin()) {
                    it
                } else {
                    it.and(ADDRESS.USER_ID.eq(auth.getUid()))
                }
            }
            .fetchSingle()
    }

    @Transactional
    fun save(address: AddressRecord): AddressRecord {
        if (address.id == null) {
            if (!auth.isSuperAdmin() || address.userId == null) {
                address.userId = auth.getUid()
            }
            return dsl.insertInto(ADDRESS)
                .set(address)
                .returning()
                .fetchOne()!!
        } else {
            val existing = findById(address.id!!)
            existing.from(address)
            existing.store()
            return existing
        }
    }

    @Transactional
    fun deleteById(id: Long) {
        val condition = ADDRESS.ID.eq(id).let {
            if (auth.isSuperAdmin()) {
                it
            } else {
                it.and(ADDRESS.USER_ID.eq(auth.getUid()))
            }
        }
        if (dsl.deleteFrom(ADDRESS).where(condition).execute() == 0) {
            throw NoDataFoundException()
        }
    }
}
