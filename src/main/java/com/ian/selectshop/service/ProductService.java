package com.ian.selectshop.service;

import com.ian.selectshop.dto.ProductRequestDto;
import com.ian.selectshop.dto.ProductResponseDto;
import com.ian.selectshop.dto.MyPriceRequestDto;
import com.ian.selectshop.entity.Product;
import com.ian.selectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public static final int MIN_MY_PRICE = 100;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = productRepository.save(new Product(requestDto));

        return new ProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, MyPriceRequestDto requestDto) {
        int price = requestDto.getMyprice();
        if (price < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 "
                    + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("해당 상품을 찾을 수 없습니다."));

        product.updateMyPrice(price);

        return new ProductResponseDto(product);
    }
}