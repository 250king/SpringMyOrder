package com.king250.order.api.module.delivery

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.integration.kd100.CreateOrderRequest
import com.king250.order.api.integration.kd100.Kd100Service
import com.king250.order.api.module.address.AddressService
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.enums.DeliveryCompany
import com.king250.order.jooq.enums.GroupRole
import com.king250.order.jooq.enums.ListStatus
import com.king250.order.jooq.tables.records.DeliveryRecord
import com.king250.order.jooq.tables.references.*
import kotlinx.coroutines.*
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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

    private val creator = USER.`as`("creator")

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
            .and(LIST.STATUS.eq(ListStatus.ARRIVED))
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
    internal fun isInGroup(userId: Long, creatorId: Long): Boolean {

    }

    fun findAll(request: QueryDeliveryRequest): Page<Record> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>().apply {
            if (!auth.isSuperAdmin()) {
                add(DELIVERY.USER_ID.eq(auth.getUid()).or(DELIVERY.CREATOR_ID.eq(auth.getUid())))
            } else {
                request.userId?.let {
                    add(DELIVERY.USER_ID.eq(it))
                }
                request.creatorId?.let {
                    add(DELIVERY.CREATOR_ID.eq(it))
                }
            }
            request.company?.let {
                add(DELIVERY.COMPANY.eq(it))
            }
            request.status?.let {
                add(DELIVERY.STATUS.eq(it))
            }
            request.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                add(DELIVERY.NAME.containsIgnoreCase(kw)
                    .or(DELIVERY.NAME.containsIgnoreCase(kw))
                    .or(DELIVERY.PHONE.containsIgnoreCase(kw))
                    .or(DELIVERY.ADDRESS.containsIgnoreCase(kw))
                    .or(DELIVERY.TRACKING_NUMBER.containsIgnoreCase(kw))
                )
            }
        }
        val sortMap = mapOf(
            "id" to DELIVERY.ID,
            "user_id" to DELIVERY.USER_ID,
            "creator_id" to DELIVERY.CREATOR_ID,
            "name" to DELIVERY.NAME,
            "phone" to DELIVERY.PHONE,
            "address" to DELIVERY.ADDRESS,
            "company" to DELIVERY.COMPANY,
            "status" to DELIVERY.STATUS,
            "created_at" to DELIVERY.CREATED_AT,
            "updated_at" to DELIVERY.UPDATED_AT,
        )
        val total = dsl.fetchCount(DELIVERY, conditions)
        val records = dsl.select(*USER.fields(), *creator.fields(), *DELIVERY.fields())
            .from(DELIVERY)
            .join(USER)
            .on(USER.ID.eq(DELIVERY.USER_ID))
            .join(creator)
            .on(creator.ID.eq(DELIVERY.CREATOR_ID))
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
         return PageImpl(records, pageable, total.toLong())
    }

    fun findById(id: Long): Record {
        return dsl.select(*USER.fields(), *creator.fields(), *DELIVERY.fields())
            .from(DELIVERY)
            .join(USER)
            .on(USER.ID.eq(DELIVERY.USER_ID))
            .join(creator)
            .on(creator.ID.eq(DELIVERY.CREATOR_ID))
            .where(DELIVERY.ID.eq(id))
            .and(
                if (auth.isSuperAdmin()) {
                    DSL.noCondition()
                } else {
                    DSL.or(
                        DELIVERY.USER_ID.eq(auth.getUid()),
                        DELIVERY.CREATOR_ID.eq(auth.getUid())
                    )
                }
            ).fetchSingle()
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
            val t1 = GROUP_USER.`as`("t1")
            val t2 = GROUP_USER.`as`("t2")

            delivery.creatorId = if (auth.isSuperAdmin() && delivery.creatorId != null) {
                delivery.creatorId
            } else {
                auth.getUid()
            }
            val filter = checkListAvailable(lists)
            val inserted = dsl.insertInto(DELIVERY)
                .set(delivery)
                .returning()
                .fetchOne()!!
            insertListToDelivery(inserted.id!!, filter)
            return inserted
        } else {
            dsl.attach(delivery)
            delivery.store()
            return delivery
        }
    }

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
            throw AccessDeniedException("Some lists are not accessible")
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
