package com.king250.order.api.config

import com.king250.order.api.common.AuditListener
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultRecordListenerProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JooqConfig {
    @Bean
    fun configuration(dataSource: DataSource): DefaultConfiguration {
        val config = DefaultConfiguration()
        config.set(dataSource)
        config.set(SQLDialect.POSTGRES)
        config.set(DefaultRecordListenerProvider(AuditListener()))
        return config
    }
}