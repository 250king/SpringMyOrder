package com.king250.order.api.module.delivery

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.integration.kd100.CreateOrderRequest
import com.king250.order.api.integration.kd100.Kd100Service
import com.king250.order.api.module.address.AddressService
import com.king250.order.jooq.enums.DeliveryCompany
import com.king250.order.jooq.enums.GroupRole
import com.king250.order.jooq.tables.records.DeliveryRecord
import com.king250.order.jooq.tables.references.DELIVERY
import com.king250.order.jooq.tables.references.DELIVERY_LIST
import com.king250.order.jooq.tables.references.GROUP_USER
import com.king250.order.jooq.tables.references.ITEM
import com.king250.order.jooq.tables.references.LIST
import io.ktor.http.parameters
import jdk.internal.org.jline.utils.AttributedStringBuilder.append
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeliveryService(
    private val dsl: DSLContext,
    private val auth: AuthService,
    private val kd100: Kd100Service,
    private val address: AddressService,
) {
    private val companyMap = mapOf(
        DeliveryCompany.SF to "shunfeng",
        DeliveryCompany.YTO to "yuantong",
        DeliveryCompany.ZTO to "zhongtong",
        DeliveryCompany.JD to "jd"
    )

    fun findById(id: Long): DeliveryRecord {
        return dsl.selectFrom(DELIVERY)
            .where(DELIVERY.ID.eq(id))
            .let {
                if (auth.isSuperAdmin()) {
                    it
                } else {
                    it.and(
                        DSL.or(
                            DELIVERY.USER_ID.eq(auth.getUid()),
                            DELIVERY.CREATOR_ID.eq(auth.getUid())
                        )
                    )
                }
            }.fetchSingle()
    }

    @Transactional
    fun save(delivery: DeliveryRecord, addressId: Long?, lists: List<Long> = emptyList()): DeliveryRecord {
        addressId?.let { id ->
            try {
                val result = address.findById(id)
                delivery.name = result.name
                delivery.phone = result.phone
                delivery.address = result.address
            } catch (_: Exception) { }
        }
        if (delivery.id == null) {
            delivery.creatorId = auth.getUid()
            val filter = checkListAvailable(lists)
            val inserted = dsl.insertInto(DELIVERY)
                .set(delivery)
                .returning()
                .fetchOne()!!
            insertListToDelivery(inserted.id!!, filter)
            return delivery
        } else {
            val existing = findById(delivery.id!!)
            delivery.from(existing)
            delivery.store()
            return delivery
        }
    }

    @Transactional
    internal fun checkListAvailable(lists: List<Long>): Set<Long?> {
        if (auth.isSuperAdmin()) {
            return lists.toSet()
        }
        val filter = dsl.select(LIST.ID)
            .from(LIST)
            .join(ITEM).on(ITEM.ID.eq(LIST.ITEM_ID))
            .join(GROUP_USER).on(GROUP_USER.GROUP_ID.eq(ITEM.GROUP_ID))
            .where(LIST.ID.`in`(lists))
            .and(GROUP_USER.USER_ID.eq(auth.getUid()))
            .and(GROUP_USER.ROLE.eq(GroupRole.OWNER))
            .fetchSet(LIST.ID)
        if (filter.size != lists.toSet().size) {
            throw AccessDeniedException("Some lists are not accessible")
        }
        return filter
    }

    @Transactional
    internal fun insertListToDelivery(deliveryId: Long, lists: Set<Long?>): Int {
        val inserts = lists.map { list ->
            dsl.insertInto(DELIVERY_LIST)
                .set(DELIVERY_LIST.DELIVERY_ID, deliveryId)
                .set(DELIVERY_LIST.LIST_ID, list)
                .onDuplicateKeyIgnore()
        }
        return dsl.batch(inserts).execute().filter { it == 1 }.size
    }

    @Transactional
    suspend fun pushDelivery(request: PushDeliveryRequest): Int {
        val result = address.findById(request.addressId)
        val filter = withContext(Dispatchers.IO) {
            dsl.selectFrom(DELIVERY)
                .where(DELIVERY.ID.`in`(request.deliveries))
                .and(
                    if (auth.isSuperAdmin()) {
                        DSL.noCondition()
                    }
                    else {
                        DSL.or(
                            DELIVERY.USER_ID.eq(auth.getUid()),
                            DELIVERY.CREATOR_ID.eq(auth.getUid())
                        )
                    }
                )
                .and(DELIVERY.NAME.isNotNull)
                .and(DELIVERY.PHONE.isNotNull)
                .and(DELIVERY.ADDRESS.isNotNull)
                .and(DELIVERY.COMPANY.isNotNull)
                .fetch()
        }
        if (filter.size != request.deliveries.toSet().size) {
            throw AccessDeniedException("Some lists are not accessible")
        }
        val total = coroutineScope {
            filter.map { delivery ->
                async {
                    val res = kd100.createOrder(CreateOrderRequest(
                        kuaidicom = companyMap[delivery.company!!]!!,
                        recManName = delivery.name!!,
                        recManMobile = delivery.phone!!,
                        recManPrintAddr = delivery.address!!,
                        sendManName = result.name!!,
                        sendManMobile = result.phone!!,
                        sendManPrintAddr = result.address!!,
                        cargo = request.type
                    ))
                }
            }.awaitAll()
        }
        return total
    }
}
