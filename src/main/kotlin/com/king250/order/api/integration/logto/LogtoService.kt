package com.king250.order.api.integration.logto

import io.ktor.client.HttpClient
import org.springframework.stereotype.Service

@Service
class LogtoService(
    ktor: HttpClient
) {
    private val client = ktor.config {
    }
}
