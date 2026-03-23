package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service

@Service
class Kd100Service(
    ktor: HttpClient,
    private val objectMapper: ObjectMapper,
    private val properties: Kd100Properties
) {
    private val client = ktor.config {
        install(Kd100SignPlugin) {
            key = properties.key
            secret = properties.secret
        }
    }

    suspend fun createOrder(data: CreateOrderRequest): Kd100Response<OrderResponse> {
        data.callBackUrl = properties.webhook
        data.salt = properties.salt
        return client.submitForm(
            url = "https://order.kuaidi100.com/order/corderapi.do",
            formParameters = parameters {
                append("method", "cOrder")
                append("param", objectMapper.writeValueAsString(data))
            }
        ).body()
    }

    fun checkSignature(data: String, sign: String): Boolean {
        val signStr = "$data${properties.salt}"
        return sign == DigestUtils.md5Hex(signStr).uppercase()
    }

    fun parseData(data: String): WebhookData {
        return objectMapper.readValue(data, WebhookData::class.java)
    }
}
