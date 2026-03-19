package com.king250.order.api

import com.king250.order.api.integration.jd.JdService
import com.king250.order.api.integration.jd.Method
import com.king250.order.jooq.enums.PaymentMethod
import com.king250.order.jooq.tables.references.PAYMENT
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@SpringBootTest
@ActiveProfiles("local")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApplicationTests(
    private val dsl: DSLContext,
    private val jd: JdService,
) {

    @Test
    fun contextLoads() {
        val payments = dsl.selectFrom(PAYMENT).fetch()
        payments.forEach { record ->
            record.requestId?.let {
                val result = runBlocking {
                    jd.getOrder(it)
                }
                result.data?.let { data ->
                    record.paidAt = data.completeTime
                    record.method = when (data.payWayEnum) {
                        Method.GUOTONG_PAY_ALIPAY -> PaymentMethod.ALIPAY
                        Method.GUOTONG_PAY_ALIPAY_SCAN -> PaymentMethod.ALIPAY
                        Method.GUOTONG_PAY_WX -> PaymentMethod.WECHAT
                        Method.GUOTONG_PAY_WX_SCAN -> PaymentMethod.WECHAT
                        Method.GUOTONG_PAY_UNIONPAY -> PaymentMethod.UNIONPAY
                        Method.GUOTONG_PAY_UNIONPAY_SCAN -> PaymentMethod.UNIONPAY
                        Method.GUOTONG_PAY_JD -> PaymentMethod.JDPAY
                        Method.GUOTONG_PAY_JD_SCAN -> PaymentMethod.JDPAY
                    }
                    record.store()
                }
            }
        }
    }

}
