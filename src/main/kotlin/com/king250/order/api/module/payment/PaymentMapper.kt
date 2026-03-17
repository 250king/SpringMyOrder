package com.king250.order.api.module.payment

import com.king250.order.api.config.MapperConfig
import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.tables.records.PaymentRecord
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.PAYMENT
import com.king250.order.jooq.tables.references.USER
import org.jooq.Record
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(config = MapperConfig::class)
interface PaymentMapper {
    fun toResponse(record: Record): PaymentResponse {
        val payment = record.into(PAYMENT)
        val user = record.into(USER)
        return mergeToResponse(payment, user)
    }

    @Mapping(target = "user", source = "userRecord")
    @Mapping(target = "id", source = "paymentRecord.id")
    @Mapping(target = "createdAt", source = "paymentRecord.createdAt")
    @Mapping(target = "updatedAt", source = "paymentRecord.updatedAt")
    fun mergeToResponse(paymentRecord: PaymentRecord, userRecord: UserRecord): PaymentResponse

    fun mapUser(record: UserRecord): UserResponse
}
