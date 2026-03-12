package com.king250.order.api.module.address

import com.king250.order.api.util.toJooq
import com.king250.order.jooq.tables.records.AddressRecord
import com.king250.order.jooq.tables.references.ADDRESS
import org.jooq.Condition
import org.jooq.DSLContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AddressService(
    private val dsl: DSLContext,
) {
    fun findAll(request: AddressQueryRequest): Page<AddressRecord> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>()
        request.id?.let {
            conditions.add(ADDRESS.ID.eq(it))
        }
        request.userId?.let {
            conditions.add(ADDRESS.USER_ID.eq(it))
        }
        request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
            conditions.add(ADDRESS.NAME.containsIgnoreCase(kw)
                .or(ADDRESS.PHONE.containsIgnoreCase(kw))
                .or(ADDRESS.ADDRESS_.containsIgnoreCase(kw))
            )
        }
        val total = dsl.fetchCount(ADDRESS, conditions)
        val records = dsl.selectFrom(ADDRESS)
            .where(conditions)
            .orderBy(pageable.sort.toJooq(emptyMap()))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): AddressRecord {
        return dsl.selectFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
            .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @Transactional
    fun save(address: AddressRecord): AddressRecord {
        try {
            if (address.id == null) {
                return dsl.insertInto(ADDRESS)
                    .set(address)
                    .returning()
                    .fetchOne()!!
            } else {
                dsl.attach(address)
                address.store()
                return address
            }
        } catch (_: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT)
        }
    }

    @Transactional
    fun deleteById(id: Long) {
        if (dsl.fetchExists(ADDRESS.where(ADDRESS.ID.eq(id)))) {
            dsl.deleteFrom(ADDRESS)
                .where(ADDRESS.ID.eq(id))
                .execute()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }
}
