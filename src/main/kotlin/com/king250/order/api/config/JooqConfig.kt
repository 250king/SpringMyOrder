package com.king250.order.api.config

import com.king250.order.api.common.listener.AuditListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JooqConfig {
    @Bean
    fun auditListener(): AuditListener {
        return AuditListener()
    }
}