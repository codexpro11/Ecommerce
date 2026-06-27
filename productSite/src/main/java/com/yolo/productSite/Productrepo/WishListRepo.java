package com.yolo.productSite.Productrepo;

import com.yolo.productSite.model.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishListRepo extends JpaRepository<WishList,Long> {
    Optional<WishList> findByProductId(int productId);
}
