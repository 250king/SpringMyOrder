package com.king250.order.api.module.address

import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AddressController(
    private val service: AddressService,
    private val mapper: AddressMapper
) {
    @GetMapping("/addresses")
    fun getAddresses(@Valid request: AddressQueryRequest): ItemResponse<AddressResponse> {
        val addresses = service.findAll(request)
        return addresses.toItem(mapper::toResponse)
    }

    @PostMapping("/addresses")
    fun createAddress(@Valid @RequestBody request: AddressCreateRequest): AddressResponse {
        val address = mapper.toEntity(request)
        return mapper.toResponse(service.save(address))
    }

    @GetMapping("/addresses/{id}")
    fun getAddressById(@PathVariable id: Long): AddressResponse {
        val address = service.findById(id)
        return mapper.toResponse(address)
    }

    @PatchMapping("/addresses/{id}")
    fun updateAddressById(@PathVariable id: Long, @Valid @RequestBody request: AddressUpdateRequest): AddressResponse {
        val address = service.findById(id)
        mapper.updateEntity(request, address)
        return mapper.toResponse(service.save(address))
    }

    @DeleteMapping("/addresses/{id}")
    fun deleteAddressById(@PathVariable id: Long) {
        service.deleteById(id)
    }
}