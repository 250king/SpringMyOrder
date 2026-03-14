package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.http.parameters
import io.ktor.serialization.jackson.jackson
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils

@Service
class Kd100Service(
    ktor: HttpClient,
    private val properties: Kd100Properties
) {
    private val client = ktor.config {
        install(ContentNegotiation) {
            jackson {
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
            }
        }
    }.apply {
        this.requestPipeline.intercept(HttpRequestPipeline.Transform) { payload ->
            if (payload is FormDataContent) {
                val params = payload.formData
                val param = params["param"] ?: ""
                val t = (System.currentTimeMillis() / 1000 * 1000).toString()
                val signStr = param + t + properties.key + properties.secret
                val sign = DigestUtils.md5DigestAsHex(signStr.toByteArray()).uppercase()
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
        return client.submitForm(
            url = "https://order.kuaidi100.com/order/corderapi.do",
            formParameters = parameters {
                append()
            }
        ).body()
    }
}
