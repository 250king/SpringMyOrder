package com.king250.order.api.integration.jd

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.request.headers
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.encodedPath
import org.springframework.stereotype.Service
import org.apache.commons.codec.digest.DigestUtils

@Suppress("UastIncorrectHttpHeaderInspection")
@Service
class JdService(
    ktor: HttpClient,
    private val properties: JdProperties
) {
    private val client = ktor.apply {
        sendPipeline.intercept(HttpSendPipeline.State) {
            val builder = context // HttpRequestBuilder
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val path = builder.url.encodedPath
            var digestString = "secretKey=${properties.secret}&timestamp=$timestamp&path=$path"

            if (builder.method == HttpMethod.Post) {
                val body = builder.body
                if (body is TextContent) {
                    digestString += "&body=${body.text}"
                }
            }

            val token = DigestUtils.sha1Hex(digestString).uppercase()
            builder.headers {
                append("accessKey", properties.key)
                append("timestamp", timestamp)
                append("token", token)
            }

            proceed()
        }
    }
}
