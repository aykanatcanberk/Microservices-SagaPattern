package com.canbe.productservice.service.impl;

import com.canbe.productservice.client.InventoryClient;
import com.canbe.productservice.dto.InventoryRequest;
import com.canbe.productservice.dto.ProductRequest;
import com.canbe.productservice.entity.Product;
import com.canbe.productservice.exception.InventoryCommunicationException;
import com.canbe.productservice.exception.ProductNotFoundException;
import com.canbe.productservice.repository.ProductRepository;
import com.canbe.productservice.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product createProduct(ProductRequest request) {
        Product newProduct = new Product();
        newProduct.setName(request.name());
        newProduct.setDescription(request.description());
        newProduct.setPrice(request.price());

        Product savedProduct = productRepository.save(newProduct);

        try {
            InventoryRequest inventoryRequest = new InventoryRequest(savedProduct.getId(), request.stock());
            inventoryClient.createInventory(inventoryRequest);
        } catch (Exception e) {
            productRepository.delete(savedProduct);
            log.error("Stok oluşturulamadığı için ürün silindi. Ürün ID: {}", savedProduct.getId(), e);

            throw new InventoryCommunicationException("Ürün kaydedildi ancak stok oluşturulamadı. İşlem geri alındı. Hata: " + e.getMessage());
        }

        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Güncellenecek ürün bulunamadı. ID: " + id));

        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setPrice(request.price());

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Silinecek ürün bulunamadı. ID: " + id);
        }

        try {
            inventoryClient.deleteInventoryByProductId(id);
        } catch (Exception e) {
            log.warn("Inventory kaydı silinirken hata oluştu: " + "productId={}", id, e);
        }
        productRepository.deleteById(id);
    }
}