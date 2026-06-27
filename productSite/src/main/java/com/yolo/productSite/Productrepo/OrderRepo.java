package com.yolo.productSite.Productrepo;

import com.yolo.productSite.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<Order,Long>
{
    /**
     * Find all orders with eager loading of order items and products
     * This prevents LazyInitializationException
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderitems oi " +
            "LEFT JOIN FETCH oi.p")
    List<Order> findAllWithItems();

    /**
     * Find order by order ID with eager loading
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderitems oi " +
            "LEFT JOIN FETCH oi.p " +
            "WHERE o.orderid = :orderId")
    Optional<Order> findByOrderidWithItems(String orderId);

    /**
     * Find orders by customer email with eager loading
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderitems oi " +
            "LEFT JOIN FETCH oi.p " +
            "WHERE o.email = :email")
    List<Order> findByEmailWithItems(String email);
}

