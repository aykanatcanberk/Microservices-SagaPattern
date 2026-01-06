package com.canbe.orderservice.event;

public record OrderCancelledEvent(
        Long orderId,
        String reason
) {}