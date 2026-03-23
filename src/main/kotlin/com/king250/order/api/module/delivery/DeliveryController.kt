package com.king250.order.api.module.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import com.king250.order.jooq.tables.records.DeliveryRecord
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class DeliveryController(
    private val service: DeliveryService,
    private val mapper: DeliveryMapper,
    private val objectMapper: ObjectMapper
) {
    @PostMapping("/admin/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateDeliveryRequest): DeliveryResponse {
        val delivery = mapper.toEntity(request)
        return mapper.toResponse(service.save(delivery, request.addressId, request.lists))
    }

    @PostMapping("/admin/deliveries/push")
    suspend fun pushDelivery(@Valid @RequestBody request: PushDeliveryRequest): ObjectNode {
        val result = service.pushDelivery(request)
        return objectMapper.createObjectNode().apply {
            put("total", result)
        }
    }

    @GetMapping("/webhook/delivery")
    fun webhook(
        @RequestParam("taskId") taskId: String,
        @RequestParam("param") param: String,
        @RequestParam("sign") sign: String
    ) {
        service.webhook(taskId, param, sign)
    }

    @GetMapping("/deliveries")
    fun findAll(@Valid @ParameterObject request: QueryDeliveryRequest): ItemResponse<DeliveryResponse> {
        val deliveries = service.findAll(request)
        return deliveries.toItem(mapper::toResponse)
    }

    @GetMapping("/deliveries/{deliveryId}")
    @PreAuthorize("@auth.isSelfDelivery(#deliveryId)")
    fun findById(@PathVariable deliveryId: Long): DeliveryResponse {
        val delivery = service.findById(deliveryId)
        return mapper.toResponse(delivery)
    }

    @PostMapping("/deliveries/{deliveryId}")
    @PreAuthorize("@auth.isSelfDelivery(#deliveryId)")
    fun updateDelivery(
        @PathVariable deliveryId: Long,
        @Valid @RequestBody request: UpdateDeliveryRequest
    ): DeliveryResponse {
        val delivery = service.findById(deliveryId) as DeliveryRecord
        mapper.updateEntity(request, delivery)
        return mapper.toResponse(service.save(delivery, request.addressId))
    }
}
