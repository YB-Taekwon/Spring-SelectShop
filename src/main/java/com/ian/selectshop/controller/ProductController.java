package com.ian.selectshop.controller;

import com.ian.selectshop.dto.ProductRequestDto;
import com.ian.selectshop.dto.ProductResponseDto;
import com.ian.selectshop.dto.MyPriceRequestDto;
import com.ian.selectshop.security.UserDetailsImpl;
import com.ian.selectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ProductResponseDto createProduct(
            @RequestBody ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return productService.createProduct(requestDto, userDetails.getUser());
    }

    // 관심 상품 조회하기
    @GetMapping("/products")
    public Page<ProductResponseDto> getProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 응답 보내기
        return productService.getProducts(userDetails.getUser(),  page-1, size, sortBy, isAsc);
    }

    @PutMapping("/products/{id}")
    public ProductResponseDto updateProduct(@PathVariable Long id, @RequestBody MyPriceRequestDto requestDto) {
        return productService.updateProduct(id, requestDto);
    }

    // 관리자 조회
    @GetMapping("/admin/products")
    public List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts();
    }
}
