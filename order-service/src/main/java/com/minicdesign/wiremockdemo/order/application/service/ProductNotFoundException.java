package com.minicdesign.wiremockdemo.order.application.service;

@SuppressWarnings("serial")
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
