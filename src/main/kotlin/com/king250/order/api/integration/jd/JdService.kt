package com.king250.order.api.integration.jd

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.jackson.*
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@Suppress("UastIncorrectHttpHeaderInspection")
class JdService(
    ktor: HttpClient,
    private val properties: JdProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = ktor.config {
        defaultRequest {
            url("https://openapi.duolabao.com/v1")
        }
        install(ContentNegotiation) {
            jackson {
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
            }
        }
    }.apply {
        sendPipeline.intercept(HttpSendPipeline.State) {
            val builder = context // HttpRequestBuilder
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val path = builder.url.encodedPath
            var signStr = "secretKey=${properties.secret}&timestamp=$timestamp&path=$path"
            if (builder.method == HttpMethod.Post) {
                val body = builder.body
                if (body is TextContent) {
                    signStr += "&body=${body.text}"
                }
            }
            val token = DigestUtils.sha1Hex(signStr).uppercase()
            builder.headers {
                append("accessKey", properties.key)
                append("timestamp", timestamp)
                append("token", token)
            }
            proceed()
        }
    }

    suspend fun getPayUrl(request: CreateUrlRequest): JdResponse<CreateUrlResponse> {
        request.customerNum = properties.customerId
        request.shopNum = properties.shopId
        request.callbackUrl = properties.webhook
        try {
            return client.post("/customer/order/payurl/create") {
                setBody(request)
            }.body()
        } catch (e: Exception) {
            log.error("Failed to create pay url for requestId ${request.requestNum} and amount ${request.amount}", e)
            throw e
        }
    }
}
