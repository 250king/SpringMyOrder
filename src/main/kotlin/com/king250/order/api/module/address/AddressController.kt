package com.king250.order.api.module.address

import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class AddressController(
    private val service: AddressService,
    private val mapper: AddressMapper,
) {
    @GetMapping("/addresses")
    fun getAddresses(@Valid request: QueryAddressRequest): ItemResponse<AddressResponse> {
        val addresses = service.findAll(request)
        return addresses.toItem(mapper::toResponse)
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAddress(@Valid @RequestBody request: CreateAddressRequest): AddressResponse {
        val address = mapper.toEntity(request)
        return mapper.toResponse(service.save(address))
    }

    @GetMapping("/addresses/{addressId}")
    fun getAddressById(@PathVariable addressId: Long): AddressResponse {
        val address = service.findById(addressId)
        return mapper.toResponse(address)
    }

    @PatchMapping("/addresses/{addressId}")
    fun updateAddressById(@PathVariable addressId: Long, @Valid @RequestBody request: UpdateAddressRequest): AddressResponse {
        val address = service.findById(addressId)
        mapper.updateEntity(request, address)
        return mapper.toResponse(service.save(address))
    }

    @DeleteMapping("/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAddressById(@PathVariable addressId: Long) {
        service.deleteById(addressId)
    }
}