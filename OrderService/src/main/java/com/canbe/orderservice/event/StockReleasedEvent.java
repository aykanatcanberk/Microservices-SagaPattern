package com.canbe.orderservice.event;

public record StockReleasedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        String reason
) {}