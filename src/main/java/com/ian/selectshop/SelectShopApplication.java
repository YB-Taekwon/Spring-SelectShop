package com.ian.selectshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SelectShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelectShopApplication.class, args);
    }

}
