package com.canbe.productservice.service;

import com.canbe.productservice.dto.ProductRequest;
import com.canbe.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);

    Product createProduct(ProductRequest request);

    Product updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}