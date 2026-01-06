package com.canbe.orderservice.service;

import com.canbe.orderservice.dto.OrderRequest;
import com.canbe.orderservice.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> getAllOrders();

    Optional<Order> getOrderById(Long id);

    List<Order> getOrdersByCustomerEmail(String email);

    Order createOrder(OrderRequest request);

    Order updateOrder(Long id, OrderRequest request);

    Order updateOrderStatus(Long id);

    String siparisVer(Long productId);

    void deleteOrder(Long id);
}