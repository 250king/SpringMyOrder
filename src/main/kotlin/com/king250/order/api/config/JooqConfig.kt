package com.king250.order.api.config

import org.jooq.Field
import org.jooq.RecordContext
import org.jooq.RecordListener
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultRecordListenerProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
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

    @Suppress("UNCHECKED_CAST")
    private class AuditListener : RecordListener {
        override fun insertStart(ctx: RecordContext) {
            val record = ctx.record()
            val now = Instant.now()
            record.field("created_at")?.let { field ->
                record.set(field as Field<Instant>, now)
            }
            record.field("updated_at")?.let { field ->
                record.set(field as Field<Instant>, now)
            }
        }

        override fun updateStart(ctx: RecordContext) {
            val record = ctx.record()
            record.field("updated_at")?.let { field ->
                record.set(field as Field<Instant>, Instant.now())
            }
        }
    }
}