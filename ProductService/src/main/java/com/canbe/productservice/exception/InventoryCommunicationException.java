package com.canbe.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InventoryCommunicationException extends RuntimeException {
    public InventoryCommunicationException(String message) {
        super(message);
    }
}