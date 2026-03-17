package com.king250.order.api.module.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(
    private val service: PaymentService,
    private val mapper: PaymentMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/payments")
    fun findAll(@Valid request: QueryPaymentRequest): ItemResponse<PaymentResponse> {
        val payments = service.findAll(request)
        return payments.toItem(mapper::toResponse)
    }

    @GetMapping("/payments/{paymentId}")
    @PreAuthorize("@auth.isSelfPayment(#paymentId)")
    fun findById(@PathVariable paymentId: Long): PaymentResponse {
        val payment = service.findById(paymentId)
        return mapper.toResponse(payment)
    }

    @PostMapping("/payments/{paymentId}/pay")
    @PreAuthorize("@auth.isSelfPayment(#paymentId)")
    suspend fun pay(@PathVariable paymentId: Long): ObjectNode {
        val result = service.payRequest(paymentId)
        return objectMapper.createObjectNode().apply {
            put("url", result)
        }
    }
}
