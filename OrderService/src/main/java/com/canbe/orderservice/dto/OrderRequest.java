package com.canbe.orderservice.dto;

import java.math.BigDecimal;

public record OrderRequest(
        Long productId,
        Integer quantity,
        BigDecimal totalPrice,
        String customerName,
        String customerEmail
){

}