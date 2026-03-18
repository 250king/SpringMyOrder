package com.king250.order.api.integration.jd

import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.codec.digest.DigestUtils

class JdPluginConfig {
    var key: String = ""
    var secret: String = ""
    var mapper: ObjectMapper? = null
}

@Suppress("UastIncorrectHttpHeaderInspection")
val JdSignPlugin = createClientPlugin("JdAuth", ::JdPluginConfig) {
    val accessKey = pluginConfig.key
    val secretKey = pluginConfig.secret
    val mapper = pluginConfig.mapper ?: ObjectMapper()

    transformRequestBody { request, _, _ ->
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val path = request.url.encodedPath
        var signStr = "secretKey=$secretKey&timestamp=$timestamp&path=$path"
        if (request.method == HttpMethod.Post) {
            val jsonBody = mapper.writeValueAsString(request.body)
            signStr += "&body=$jsonBody"
            val token = DigestUtils.sha1Hex(signStr).uppercase()
            request.headers {
                append("accessKey", accessKey)
                append("timestamp", timestamp)
                append("token", token)
            }
            TextContent(jsonBody, ContentType.Application.Json)
        } else {
            if (request.method == HttpMethod.Get) {
                val token = DigestUtils.sha1Hex(signStr).uppercase()
                request.headers {
                    append("accessKey", accessKey)
                    append("timestamp", timestamp)
                    append("token", token)
                }
            }
            null
        }
    }
}