package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
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
        install(Kd100SignPlugin) {
            key = properties.key
            secret = properties.secret
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
