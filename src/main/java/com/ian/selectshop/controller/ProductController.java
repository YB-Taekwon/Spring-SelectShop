package com.ian.selectshop.controller;

import com.ian.selectshop.dto.ProductRequestDto;
import com.ian.selectshop.dto.ProductResponseDto;
import com.ian.selectshop.dto.MyPriceRequestDto;
import com.ian.selectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping()
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto) {
        return productService.createProduct(requestDto);
    }

    @PutMapping("/{id}")
    public ProductResponseDto updateProduct(@PathVariable Long id, @RequestBody MyPriceRequestDto requestDto) {
        return productService.updateProduct(id, requestDto);
    }
}
