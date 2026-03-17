package com.king250.order.api.module.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import com.king250.order.jooq.tables.records.DeliveryRecord
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class DeliveryController(
    private val service: DeliveryService,
    private val mapper: DeliveryMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/deliveries")
    fun findAll(request: QueryDeliveryRequest): ItemResponse<DeliveryResponse> {
        val deliveries = service.findAll(request)
        return deliveries.toItem(mapper::toResponse)
    }

    @PostMapping("/deliveries")
    @PreAuthorize("@auth.isAdminList(#request.lists)")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateDeliveryRequest): DeliveryResponse {
        val delivery = mapper.toEntity(request)
        return mapper.toResponse(service.save(delivery, request.addressId, request.lists))
    }

    @PostMapping("/deliveries/push")
    @PreAuthorize("@auth.isAdminDeliveries(#request.deliveries)")
    suspend fun pushDelivery(request: PushDeliveryRequest): ObjectNode {
        val result = service.pushDelivery(request)
        return objectMapper.createObjectNode().apply {
            put("total", result)
        }
    }

    @GetMapping("/deliveries/{deliveryId}")
    @PreAuthorize("@auth.isSelfDelivery(#deliveryId)")
    fun findById(@PathVariable deliveryId: Long): DeliveryResponse {
        val delivery = service.findById(deliveryId)
        return mapper.toResponse(delivery)
    }

    @PostMapping("/deliveries/{deliveryId}")
    @PreAuthorize("@auth.isSelfDelivery(#deliveryId)")
    fun updateDelivery(@PathVariable deliveryId: Long, @RequestBody request: UpdateDeliveryRequest): DeliveryResponse {
        val delivery = service.findById(deliveryId) as DeliveryRecord
        mapper.updateEntity(request, delivery)
        return mapper.toResponse(service.save(delivery, request.addressId))
    }
}
