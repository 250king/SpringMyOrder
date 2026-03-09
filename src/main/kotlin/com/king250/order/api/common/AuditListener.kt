@file:Suppress("UNCHECKED_CAST")

package com.king250.order.api.common

import com.king250.order.jooq.tables.records.UserRecord
import org.jooq.RecordContext
import org.jooq.RecordListener
import java.time.Instant

class AuditListener : RecordListener {
    override fun insertStart(ctx: RecordContext) {
        val record = ctx.record()
        if (record is UserRecord) {
            val now = Instant.now()
            record.createdAt = now
            record.updatedAt = now
        }
    }

    override fun updateStart(ctx: RecordContext) {
        val record = ctx.record()

        if (record is UserRecord) {
            record.updatedAt = Instant.now()
        }
    }
}