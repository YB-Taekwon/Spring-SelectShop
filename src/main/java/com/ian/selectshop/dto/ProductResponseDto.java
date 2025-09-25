package com.ian.selectshop.dto;

import com.ian.selectshop.entity.Product;
import com.ian.selectshop.entity.ProductFolder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String title;
    private String link;
    private String image;
    private int lprice;
    private int myprice;
    private List<FolderResponseDto> productFolderList = new ArrayList<>();

    public ProductResponseDto(Product product) {
        id = product.getId();
        title = product.getTitle();
        link = product.getLink();
        image = product.getImage();
        lprice = product.getLprice();
        myprice = product.getMyprice();

        for (ProductFolder productFolder : product.getProductFolderList()) {
            productFolderList.add(new FolderResponseDto(productFolder.getFolder()));
        }
    }
}