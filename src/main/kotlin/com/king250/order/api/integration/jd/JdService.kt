package com.king250.order.api.integration.jd

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JdService(
    ktor: HttpClient,
    private val properties: JdProperties,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = ktor.config {
        defaultRequest {
            url("https://openapi.duolabao.com/v1/")
        }
        install(JdSignPlugin) {
            key = properties.key
            secret = properties.secret
            mapper = objectMapper
        }
    }

    suspend fun getPayUrl(request: CreateUrlRequest): JdResponse<CreateUrlResponse> {
        request.customerNum = properties.customerId
        request.shopNum = properties.shopId
        request.callbackUrl = properties.webhook
        try {
            return client.post("customer/order/payurl/create") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            log.error("Failed to create pay url for requestId ${request.requestNum} and amount ${request.amount}", e)
            throw e
        }
    }
}
