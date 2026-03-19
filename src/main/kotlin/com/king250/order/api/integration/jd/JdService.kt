package com.king250.order.api.integration.jd

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service

@Service
class JdService(
    ktor: HttpClient,
    private val properties: JdProperties,
    private val objectMapper: ObjectMapper
) {
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

    suspend fun getOrder(requestId: String): JdResponse<OrderResponse> {
        return client.get("customer/order/payresult/${properties.customerId}/${properties.shopId}/$requestId")
            .body()
    }

    suspend fun getPayUrl(request: CreateUrlRequest): JdResponse<UrlResponse> {
        request.customerNum = properties.customerId
        request.shopNum = properties.shopId
        request.callbackUrl = properties.webhook
        return client.post("customer/order/payurl/create") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    fun checkSignature(timestamp: String, token: String): Boolean {
        val signStr = "secretKey=${properties.secret}&timestamp=${timestamp}"
        return token == DigestUtils.sha1Hex(signStr).uppercase()
    }
}
