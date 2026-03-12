package com.king250.order.api.module.address

import com.king250.order.api.config.MapperConfig
import com.king250.order.jooq.tables.records.AddressRecord
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget

@Mapper(config = MapperConfig::class)
interface AddressMapper {
    fun toResponse(address: AddressRecord): AddressResponse

    fun toEntity(request: CreateAddressRequest): AddressRecord

    fun updateEntity(request: UpdateAddressRequest, @MappingTarget address: AddressRecord): AddressRecord
}
