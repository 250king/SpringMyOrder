package com.king250.order.api.module.payment

import com.king250.order.api.integration.auth.AuthService
import com.king250.order.api.integration.jd.CreateUrlRequest
import com.king250.order.api.integration.jd.JdService
import com.king250.order.api.integration.jd.Method
import com.king250.order.api.util.toJooq
import com.king250.order.jooq.enums.PaymentMethod
import com.king250.order.jooq.tables.references.PAYMENT
import com.king250.order.jooq.tables.references.USER
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class PaymentService(
    private val dsl: DSLContext,
    private val jd: JdService,
    private val auth: AuthService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findAll(request: QueryPaymentRequest): Page<Record> {
        val pageable = request.toPageable()
        val conditions = mutableListOf<Condition>().apply {
            request.id?.let { add(PAYMENT.ID.eq(it)) }
            request.type?.let { add(PAYMENT.TYPE.eq(it)) }
            request.referenceId?.let { add(PAYMENT.REFERENCE_ID.eq(it)) }
            request.currency?.let { add(PAYMENT.CURRENCY.eq(it)) }
            request.method?.let { add(PAYMENT.METHOD.eq(it)) }
            request.paid?.let {
                if (it) {
                    add(PAYMENT.PAID_AT.isNotNull)
                } else {
                    add(PAYMENT.PAID_AT.isNull)
                }
            }
            if (!auth.isAdmin()) {
                add(PAYMENT.USER_ID.eq(auth.getUid()))
            } else if (request.userId != null) {
                add(PAYMENT.USER_ID.eq(request.userId))
            }
        }
        val sortMap = mapOf(
            "id" to PAYMENT.ID,
            "user_id" to PAYMENT.USER_ID,
            "type" to PAYMENT.TYPE,
            "reference_id" to PAYMENT.REFERENCE_ID,
            "amount" to PAYMENT.AMOUNT,
            "currency" to PAYMENT.CURRENCY,
            "method" to PAYMENT.METHOD,
            "created_at" to PAYMENT.CREATED_AT,
            "updated_at" to PAYMENT.UPDATED_AT,
            "paid_at" to PAYMENT.PAID_AT,
        )
        val total = dsl.fetchCount(PAYMENT, conditions)
        val records = dsl.select(*PAYMENT.fields(), *USER.fields())
            .from(PAYMENT)
            .join(USER).on(PAYMENT.USER_ID.eq(USER.ID))
            .where(conditions)
            .orderBy(pageable.sort.toJooq(sortMap))
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch()
        return PageImpl(records, pageable, total.toLong())
    }

    fun findById(paymentId: Long): Record {
        return dsl.select(*PAYMENT.fields(), *USER.fields())
            .from(PAYMENT)
            .join(USER).on(PAYMENT.USER_ID.eq(USER.ID))
            .where(PAYMENT.ID.eq(paymentId))
            .fetchSingle()
    }

    suspend fun webhook(timestamp: String, token: String, requestId: String) {
        if (!jd.checkSignature(timestamp, token)) {
            throw AuthorizationDeniedException("Invalid signature")
        }
        val result = jd.getOrder(requestId)
        if (result.result != "success") {
            log.error("Failed to get order for request $requestId: $result")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to get order")
        }
        result.data?.let {
            val payment = dsl.selectFrom(PAYMENT).where(PAYMENT.ID.eq(PAYMENT.ID)).fetchSingle()
            payment.paidAt = it.completeTime
            payment.method = when (it.payWayEnum) {
                Method.GUOTONG_PAY_ALIPAY, Method.GUOTONG_PAY_ALIPAY_SCAN -> PaymentMethod.ALIPAY
                Method.GUOTONG_PAY_WX, Method.GUOTONG_PAY_WX_SCAN -> PaymentMethod.WECHAT
                Method.GUOTONG_PAY_UNIONPAY, Method.GUOTONG_PAY_UNIONPAY_SCAN -> PaymentMethod.UNIONPAY
                Method.GUOTONG_PAY_JD, Method.GUOTONG_PAY_JD_SCAN -> PaymentMethod.JDPAY
            }
            payment.store()
        }
    }

    suspend fun payRequest(paymentId: Long): String? {
        val record = findById(paymentId)
        val payment = record.into(PAYMENT)
        if (payment.paidAt != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Payment paid at ${payment.paidAt}")
        }
        val time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        val amount = payment.let { p ->
            val amount = p.amount ?: BigDecimal.ZERO
            val rate = p.currencyRate ?: BigDecimal.ONE
            val feeRate = "1.0038".toBigDecimal()
            (amount * rate * feeRate).setScale(2, RoundingMode.HALF_UP)
        }
        val res = jd.getPayUrl(CreateUrlRequest(
            "$time${payment.id.toString().padStart(18, '0')}",
            amount.toString()
        ))
        if (res.result != "success") {
            log.error("Failed to get payment url for payment $paymentId: $res")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment payment failed")
        }
        payment.requestId = "$time${payment.id.toString().padStart(18, '0')}"
        dsl.attach(payment)
        payment.store()
        return res.data?.url
    }
}
