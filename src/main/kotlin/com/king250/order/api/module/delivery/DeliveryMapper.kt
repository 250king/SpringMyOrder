package com.king250.order.api.module.delivery

import com.king250.order.api.config.MapperConfig
import com.king250.order.api.module.user.UserResponse
import com.king250.order.jooq.tables.records.DeliveryRecord
import com.king250.order.jooq.tables.references.DELIVERY
import com.king250.order.jooq.tables.references.USER
import org.jooq.Record
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface DeliveryMapper {
    fun toEntity(delivery: CreateDeliveryRequest): DeliveryRecord

    fun updateEntity(request: UpdateDeliveryRequest, @MappingTarget delivery: DeliveryRecord): DeliveryRecord

    fun toResponse(record: Record): DeliveryResponse {
        val response = record.into(DELIVERY).into(DeliveryResponse::class.java)
        return response.copy(
            user = mapUser(record),
            creator = mapCreator(record)
        )
    }

    fun mapUser(r: Record): UserResponse {
        return r.into(USER).into(UserResponse::class.java)
    }

    fun mapCreator(r: Record): UserResponse {
        val creatorAlias = USER.`as`("creator")
        return r.into(creatorAlias).into(UserResponse::class.java)
    }
}
