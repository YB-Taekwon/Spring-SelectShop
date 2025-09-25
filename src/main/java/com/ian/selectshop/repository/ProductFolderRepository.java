package com.ian.selectshop.repository;

import com.ian.selectshop.entity.Folder;
import com.ian.selectshop.entity.Product;
import com.ian.selectshop.entity.ProductFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductFolderRepository extends JpaRepository<ProductFolder, Long> {
    Optional<ProductFolder> findByProductAndFolder(Product product, Folder folder);
}