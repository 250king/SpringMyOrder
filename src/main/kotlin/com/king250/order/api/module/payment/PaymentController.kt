package com.king250.order.api.module.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.king250.order.api.common.ItemResponse
import com.king250.order.api.util.toItem
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Suppress("UastIncorrectHttpHeaderInspection")
@RestController
class PaymentController(
    private val service: PaymentService,
    private val mapper: PaymentMapper,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/payments")
    fun findAll(@Valid @ParameterObject request: QueryPaymentRequest): ItemResponse<PaymentResponse> {
        val payments = service.findAll(request)
        return payments.toItem(mapper::toResponse)
    }

    @GetMapping("/payments/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun webhook(
        @RequestHeader("timestamp") timestamp: String,
        @RequestHeader("token") token: String,
        @RequestParam requestNum: String,
    ) {
        service.webhook(timestamp, token, requestNum)
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
