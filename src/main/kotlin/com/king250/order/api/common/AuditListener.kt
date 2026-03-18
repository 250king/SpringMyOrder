@file:Suppress("UNCHECKED_CAST")

package com.king250.order.api.common

import org.jooq.Field
import org.jooq.RecordContext
import org.jooq.RecordListener
import java.time.Instant

class AuditListener : RecordListener {
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