package com.king250.order.api.integration.kd100

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Status(@get:JsonValue val code: Int) {
    SUCCESS(0),
    ACCEPTED(1),
    PICKING_UP(2),
    CANCELLED_BY_USER(9),
    PICKED_UP(10),
    PICKUP_FAILED(11),
    SIGNED(13),
    ABNORMAL_SIGNED(14),
    CANCELLED(99),
    IN_TRANSIT(101),
    WEIGHT_MODIFIED(155),
    ORDER_RESURRECTED(166),
    WAYBILL_GENERATED(200),
    WAYBILL_FAILED(201),
    DELIVERING(400),
    ORDER_FAILED(610),
    UNKNOWN(-1);

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: Int): Status {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}