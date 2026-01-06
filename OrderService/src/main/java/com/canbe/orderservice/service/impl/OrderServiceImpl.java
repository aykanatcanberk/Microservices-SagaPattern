package com.canbe.orderservice.service.impl;

import com.canbe.orderservice.client.ProductClient;
import com.canbe.orderservice.dto.OrderRequest;
import com.canbe.orderservice.entity.Order;
import com.canbe.orderservice.enums.OrderStatus;
import com.canbe.orderservice.event.OrderEvent;
import com.canbe.orderservice.exception.OrderNotFoundException;
import com.canbe.orderservice.repository.OrderRepository;
import com.canbe.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order newOrder = new Order();
        newOrder.setProductId(request.productId());
        newOrder.setQuantity(request.quantity());
        newOrder.setTotalPrice(request.totalPrice());
        newOrder.setCustomerName(request.customerName());
        newOrder.setCustomerEmail(request.customerEmail());
        newOrder.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(newOrder);

        // Kafka Event Gönderimi
        try {
            OrderEvent event = new OrderEvent(saved.getId(), saved.getProductId(), saved.getQuantity(),
                    saved.getTotalPrice(), saved.getCustomerEmail());

            kafkaTemplate.send("order-placed", event);

            log.info("Order-placed event başarıyla gönderildi: orderId={}", saved.getId());
        } catch (Exception e) {
            log.error("Order kaydedildi ancak Kafka event gönderilemedi: orderId={}", saved.getId(), e);
        }

        return saved;
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Güncellenecek sipariş bulunamadı. ID: " + id));

        existingOrder.setProductId(request.productId());
        existingOrder.setQuantity(request.quantity());
        existingOrder.setTotalPrice(request.totalPrice());
        existingOrder.setCustomerName(request.customerName());
        existingOrder.setCustomerEmail(request.customerEmail());

        return orderRepository.save(existingOrder);
    }

    @Override
    public Order updateOrderStatus(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Sipariş bulunamadı: " + id));

        // order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "urunFallback")
    public String siparisVer(Long productId) {
        productClient.getProductById(productId);
        return "Sipariş oluşturuldu, ürün onaylandı.";
    }

    // FALLBACK METODU
    // Circuit Breaker devreye girdiğinde veya hata aldığında bu metod çalışır.
    public String urunFallback(Long productId, Throwable t) {
        log.error("Product servisine ulaşılamadı. ProductID: {}, Hata: {}", productId, t.getMessage());
        return "Şu anda ürün servisine ulaşılamıyor, lütfen daha sonra tekrar deneyiniz. (Fallback)";
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Silinecek sipariş bulunamadı: " + id);
        }
        orderRepository.deleteById(id);
    }
}