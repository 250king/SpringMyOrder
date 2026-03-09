package com.king250.order.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KtorConfig {
    @Bean(destroyMethod = "close")
    fun ktorHttpClient(springObjectMapper: ObjectMapper): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                register(io.ktor.http.ContentType.Application.Json, JacksonConverter(springObjectMapper))
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 5000
                requestTimeoutMillis = 60000
                socketTimeoutMillis = 60000
            }
        }
    }
}
