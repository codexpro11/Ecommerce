package com.yolo.productSite.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CRITICAL: Use EAGER fetching for the product to avoid LazyInitializationException
     * Or use JOIN FETCH in queries
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private product p;

    @Column(nullable = false)
    private int Quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal TotalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude  // Prevent circular reference
    @EqualsAndHashCode.Exclude  // Prevent circular reference
    private Order order;
}