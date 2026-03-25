package com.king250.order.api.module.delivery

import com.king250.order.api.config.MapperConfig
import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.tables.records.DeliveryRecord
import com.king250.order.jooq.tables.records.UserRecord
import com.king250.order.jooq.tables.references.DELIVERY
import com.king250.order.jooq.tables.references.USER
import org.jooq.Record
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface DeliveryMapper {
    fun toEntity(delivery: CreateDeliveryRequest): DeliveryRecord

    fun updateEntity(request: UpdateDeliveryRequest, @MappingTarget delivery: DeliveryRecord): DeliveryRecord

    fun toResponse(record: Record): DeliveryResponse {
        val delivery = record.into(DELIVERY)
        val user = record.into(USER)
        return mergeToResponse(delivery, user)
    }

    @Mapping(target = "id", source = "delivery.id")
    @Mapping(target = "name", source = "delivery.name")
    @Mapping(target = "createdAt", source = "delivery.createdAt")
    @Mapping(target = "updatedAt", source = "delivery.updatedAt")
    fun mergeToResponse(delivery: DeliveryRecord, user: UserRecord): DeliveryResponse

    fun mapUser(record: UserRecord): UserResponse
}
