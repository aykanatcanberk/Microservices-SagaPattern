package com.canbe.productservice.dto;

public record InventoryRequest(
        Long productId,
        Integer stock
) {
}
