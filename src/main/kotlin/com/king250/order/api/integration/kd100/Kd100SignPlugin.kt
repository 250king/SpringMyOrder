package com.king250.order.api.integration.kd100

import io.ktor.client.plugins.api.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.apache.commons.codec.digest.DigestUtils

class Kd100PluginConfig {
    var key: String = ""
    var secret: String = ""
}

val Kd100SignPlugin = createClientPlugin("Kd100Auth", ::Kd100PluginConfig) {
    val key = pluginConfig.key
    val secret = pluginConfig.secret

    transformRequestBody { _, content, _ ->
        if (content is FormDataContent) {
            val params = content.formData
            val param = params["param"] ?: ""
            val t = System.currentTimeMillis().toString()
            val signStr = param + t + key + secret
            val sign = DigestUtils.md5Hex(signStr).uppercase()
            val newParams = Parameters.build {
                appendAll(params)
                append("t", t)
                append("key", key)
                append("sign", sign)
            }
            FormDataContent(newParams)
        } else {
            null
        }
    }
}