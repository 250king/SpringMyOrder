package com.king250.order.api.module.delivery

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.integration.kd100.CreateOrderRequest
import com.king250.order.api.integration.kd100.Kd100Service
import com.king250.order.api.module.address.AddressService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.enums.DeliveryCompany
import com.king250.order.jooq.tables.records.DeliveryRecord
import com.king250.order.jooq.tables.references.DELIVERY
import com.king250.order.jooq.tables.references.DELIVERY_LIST
import com.king250.order.jooq.tables.references.USER
import kotlinx.coroutines.*
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.security.authorization.AuthorizationDeniedException
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

    @Transactional
    internal fun insertListToDelivery(deliveryId: Long, lists: List<Long?>): Int {
        val inserts = lists.map { list ->
            dsl.insertInto(DELIVERY_LIST)
                .set(DELIVERY_LIST.DELIVERY_ID, deliveryId)
                .set(DELIVERY_LIST.LIST_ID, list)
                .onDuplicateKeyIgnore()
        }
        return dsl.batch(inserts).execute().filter { it == 1 }.size
    }

    fun findAll(request: QueryDeliveryRequest): Page<Record> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>().apply {
            request.company?.let { add(DELIVERY.COMPANY.eq(it)) }
            request.status?.let { add(DELIVERY.STATUS.eq(it)) }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                add(DELIVERY.NAME.containsIgnoreCase(kw)
                    .or(DELIVERY.NAME.containsIgnoreCase(kw))
                    .or(DELIVERY.PHONE.containsIgnoreCase(kw))
                    .or(DELIVERY.ADDRESS.containsIgnoreCase(kw))
                    .or(DELIVERY.TRACKING_NUMBER.containsIgnoreCase(kw))
                )
            }
            if (request.userId != null && auth.isAdminMember(request.userId!!)) {
                add(DELIVERY.USER_ID.eq(request.userId))
            } else {
                add(DELIVERY.USER_ID.eq(auth.getUid()))
            }
        }
        val sortMap = mapOf(
            "id" to DELIVERY.ID,
            "user_id" to DELIVERY.USER_ID,
            "name" to DELIVERY.NAME,
            "phone" to DELIVERY.PHONE,
            "address" to DELIVERY.ADDRESS,
            "company" to DELIVERY.COMPANY,
            "status" to DELIVERY.STATUS,
            "created_at" to DELIVERY.CREATED_AT,
            "updated_at" to DELIVERY.UPDATED_AT,
        )
        val total = dsl.fetchCount(DELIVERY, conditions)
        val records = dsl.select(*USER.fields(), *DELIVERY.fields())
            .from(DELIVERY)
            .join(USER).on(USER.ID.eq(DELIVERY.USER_ID))
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize).offset(pageable.offset)
            .fetch()
         return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): Record {
        return dsl.select(*USER.fields(), *DELIVERY.fields())
            .from(DELIVERY)
            .join(USER).on(USER.ID.eq(DELIVERY.USER_ID))
            .where(DELIVERY.ID.eq(id))
            .fetchSingle()
    }

    @Transactional
    fun save(delivery: DeliveryRecord, addressId: Long?, lists: List<Long> = emptyList()): Record {
        addressId?.let { id ->
            try {
                val result = address.findById(id)
                delivery.name = result.name
                delivery.phone = result.phone
                delivery.address = result.address
            } catch (_: Exception) { }
        }
        val id = if (delivery.id == null) {
            if (!auth.isSuperAdmin() || delivery.userId == null) {
                delivery.userId = auth.getUid()
            }
            val insertedId = dsl.insertInto(DELIVERY)
                .set(delivery)
                .returning(DELIVERY.ID)
                .fetchSingle()
                .id!!
            insertListToDelivery(insertedId, lists)
            insertedId
        } else {
            dsl.attach(delivery)
            delivery.store()
            delivery.id!!
        }
        return findById(id)
    }

    suspend fun pushDelivery(request: PushDeliveryRequest): Int {
        val result = address.findById(request.addressId)
        val filter = withContext(Dispatchers.IO) {
            dsl.selectFrom(DELIVERY)
                .where(DELIVERY.ID.`in`(request.deliveries))
                .and(
                    DSL.exists(
                        dsl.selectOne()
                            .from(DELIVERY_LIST)
                            .where(DELIVERY_LIST.DELIVERY_ID.eq(DELIVERY.ID))
                    )
                )
                .and(DELIVERY.NAME.isNotNull)
                .and(DELIVERY.PHONE.isNotNull)
                .and(DELIVERY.ADDRESS.isNotNull)
                .and(DELIVERY.COMPANY.isNotNull)
                .fetch()
        }
        if (filter.size != request.deliveries.toSet().size) {
            throw AuthorizationDeniedException("Some lists information are not incompleted")
        }
        val results = coroutineScope {
            filter.map { delivery ->
                async {
                    runCatching {
                        kd100.createOrder(CreateOrderRequest(
                            kuaidicom = companyMap[delivery.company!!]!!,
                            recManName = delivery.name!!,
                            recManMobile = delivery.phone!!,
                            recManPrintAddr = delivery.address!!,
                            sendManName = result.name!!,
                            sendManMobile = result.phone!!,
                            sendManPrintAddr = result.address!!,
                            cargo = request.type
                        ))
                    }.getOrNull()
                }
            }.awaitAll()
        }
        return withContext(Dispatchers.IO) {
            dsl.transactionResult { configuration ->
                var total = 0
                results.forEachIndexed { index, response ->
                    val delivery = filter[index]
                    if (response?.returnCode == "200") {
                        delivery.attach(configuration)
                        delivery.trackingNumber = response.data.kuaidinum
                        delivery.orderId = response.data.orderId
                        delivery.taskId = response.data.taskId
                        delivery.store()
                        total ++
                    }
                }
                total
            }
        }
    }
}
