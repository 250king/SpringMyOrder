package com.king250.order.api.integration.logto

import com.fasterxml.jackson.databind.ObjectMapper
import com.king250.order.api.common.OAuth2TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.stereotype.Service

@Service
class LogtoService(
    private val properties: LogtoProperties,
    private val authProperties: OAuth2ResourceServerProperties,
    private val objectMapper: ObjectMapper,
    private val ktor: HttpClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val url = buildString {
        append("https://")
        append(Url(authProperties.jwt.issuerUri).host)
        append("/api")
    }

    private val client = ktor.config {
        install(Auth) {
            bearer {
                loadTokens {
                    getToken()
                }
            }
        }
    }

    private suspend fun getToken(): BearerTokens? {
        return try {
            val response = ktor.submitForm(
                url = "${authProperties.jwt.issuerUri}/token",
                formParameters = parameters {
                    append("grant_type", "client_credentials")
                    append("client_id", properties.clientId)
                    append("client_secret", properties.clientSecret)
                    append("resource", "https://default.logto.app/api")
                    append("scope", "all")
                }
            ).body<OAuth2TokenResponse>()
            BearerTokens(response.accessToken, null)
        } catch (e: Exception) {
            log.error("Failed to get access token from Logto", e)
            null
        }
    }

    suspend fun getUserInfo(userId: String): UserData {
        return client.get("$url/users/$userId").body()
    }

    suspend fun addUser(user: CreateUserRequest): UserData {
        return client.post("$url/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }.body()
    }

    fun checkSignature(body: String, signature: String): Boolean {
        val mac = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, properties.key.toByteArray())
        return Hex.encodeHexString(mac.doFinal(body.toByteArray())) == signature
    }

    fun parseData(data: String): WebhookData {
        return objectMapper.readValue(data, WebhookData::class.java)
    }
}
