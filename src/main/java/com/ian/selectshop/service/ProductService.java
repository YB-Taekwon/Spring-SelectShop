package com.ian.selectshop.service;

import com.ian.selectshop.dto.ProductRequestDto;
import com.ian.selectshop.dto.ProductResponseDto;
import com.ian.selectshop.entity.Product;
import com.ian.selectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    public final ProductRepository productRepository;

    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = productRepository.save(new Product(requestDto));

        return new ProductResponseDto(product);
    }
}