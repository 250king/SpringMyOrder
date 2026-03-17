package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class Kd100Service(
    ktor: HttpClient,
    private val objectMapper: ObjectMapper,
    private val properties: Kd100Properties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = ktor.config {
        install(ContentNegotiation) {
            jackson {
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
            }
        }
    }.apply {
        requestPipeline.intercept(HttpRequestPipeline.Transform) { payload ->
            if (payload is FormDataContent) {
                val params = payload.formData
                val param = params["param"] ?: ""
                val t = System.currentTimeMillis().toString()
                val signStr = param + t + properties.key + properties.secret
                val sign = DigestUtils.md5Hex(signStr).uppercase()
                val newParams = Parameters.build {
                    appendAll(params)
                    append("t", t)
                    append("key", properties.key)
                    append("sign", sign)
                }
                proceedWith(FormDataContent(newParams))
            }
        }
    }

    suspend fun createOrder(data: CreateOrderRequest): Kd100Response<OrderResponse> {
        data.callBackUrl = properties.webhook
        try {
            return client.submitForm(
                url = "https://order.kuaidi100.com/order/corderapi.do",
                formParameters = parameters {
                    append("method", "cOrder")
                    append("param", objectMapper.writeValueAsString(data))
                }
            ).body()
        } catch (e: Exception) {
            log.error("Kd100 API call failed with data $data", e)
            throw e
        }
    }
}
