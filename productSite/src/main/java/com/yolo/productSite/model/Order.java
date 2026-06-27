package com.yolo.productSite.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderid;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate orderDate;

    /**
     * CRITICAL: Use CascadeType.ALL and fetch = FetchType.EAGER for order items
     * Or use custom queries with JOIN FETCH to avoid LazyInitializationException
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY  // Keep LAZY but use JOIN FETCH queries
    )
    @ToString.Exclude  // Prevent circular reference in toString
    @EqualsAndHashCode.Exclude  // Prevent circular reference in equals/hashCode
    private List<OrderItem> orderitems = new ArrayList<>();

    /**
     * Helper method to add order item
     */
    public void addOrderItem(OrderItem item) {
        orderitems.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove order item
     */
    public void removeOrderItem(OrderItem item) {
        orderitems.remove(item);
        item.setOrder(null);
    }
}