@file:Suppress("UNCHECKED_CAST")

package com.king250.order.api.common.listener

import org.jooq.RecordContext
import org.jooq.RecordListener
import java.time.Instant

class AuditListener : RecordListener {

    override fun insertStart(ctx: RecordContext) {
        val record = ctx.record()

        // 尝试获取 CREATED_AT 和 UPDATED_AT 字段
        // 这里用字符串匹配，可以适配所有表，只要字段名一致
        val createdAtField = record.field("created_at")
        val updatedAtField = record.field("updated_at")

        val now = Instant.now()

        createdAtField?.let {
            // 如果该字段还没被手动赋值，则自动填充
            if (record.get(it) == null) {
                record.set(it as org.jooq.Field<Instant>, now)
            }
        }

        updatedAtField?.let {
            if (record.get(it) == null) {
                record.set(it as org.jooq.Field<Instant>, now)
            }
        }
    }

    override fun updateStart(ctx: RecordContext) {
        val record = ctx.record()
        val updatedAtField = record.field("updated_at")

        updatedAtField?.let {
            // 更新操作时，强制刷新 updated_at
            record.set(it as org.jooq.Field<Instant>, Instant.now())
        }
    }
}