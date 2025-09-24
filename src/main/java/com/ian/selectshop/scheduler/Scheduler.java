package com.ian.selectshop.scheduler;

import com.ian.selectshop.entity.Product;
import com.ian.selectshop.naver.dto.ItemDto;
import com.ian.selectshop.naver.service.NaverApiService;
import com.ian.selectshop.repository.ProductRepository;
import com.ian.selectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Scheduler")
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final NaverApiService naverApiService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void updatePrice() throws InterruptedException {
        log.info("가격 업데이트 실행");

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            // 1초에 한 상품 씩 조회 (NAVER 제한)
            TimeUnit.SECONDS.sleep(1);

            // i 번째 관심 상품의 제목으로 검색 실행
            String title = product.getTitle();
            List<ItemDto> itemDtoList = naverApiService.searchItems(title);

            if (itemDtoList.size() > 0) {
                // 검색 결과 중 첫 번째 상품을 대표로 관심 상품의 정보 수정
                ItemDto itemDto = itemDtoList.get(0);
                Long id = product.getId();
                try {
                    productService.updateBySearch(id, itemDto);
                } catch (Exception e) {
                    log.error(id + " : " + e.getMessage());
                }
            }
        }
    }

}