package com.yolo.productSite.Productrepo;

import com.yolo.productSite.model.product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Productrepo extends JpaRepository<product, Integer>
{
    List<product> findByproductNameContaining(String keyword);
}
