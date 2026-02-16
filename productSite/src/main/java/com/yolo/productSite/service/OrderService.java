package com.yolo.productSite.service;

import com.yolo.productSite.Productrepo.OrderRepo;
import com.yolo.productSite.Productrepo.Productrepo;
import com.yolo.productSite.model.Order;
import com.yolo.productSite.model.OrderItem;
import com.yolo.productSite.model.dto.OrderItemRequest;
import com.yolo.productSite.model.dto.OrderItemResponse;
import com.yolo.productSite.model.dto.OrderRequest;
import com.yolo.productSite.model.dto.OrderResponse;
import com.yolo.productSite.model.product;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private Productrepo pr;

    @Autowired
    private OrderRepo or;

    @Autowired
    private VectorStore vectorStore;

    public OrderResponse placeOrder(OrderRequest request) {
        Order order = new Order();
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setOrderid(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("placed");
        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.items()) {
            product p = pr.findById(itemRequest.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (p.getStockQuantity() < itemRequest.quantity()) {
                throw new RuntimeException("Insufficient stock for " + p.getProductName());
            }

            // Update stock
            p.setStockQuantity(p.getStockQuantity() - itemRequest.quantity());
            pr.save(p);

            // Update product in vector store with new stock quantity
            updateProductInVectorStore(p);

            OrderItem oi = OrderItem.builder()
                    .p(p)
                    .Quantity(itemRequest.quantity())
                    .TotalPrice(BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(itemRequest.quantity())))
                    .order(order)
                    .build();
            orderItems.add(oi);
        }

        order.setOrderitems(orderItems);
        Order savedOrder = or.save(order);

        // Add order to vector store for chatbot queries
        addOrderToVectorStore(savedOrder);

        // Build response
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getOrderitems()) {
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getP().getProductName(),
                    item.getQuantity(),
                    item.getTotalPrice()
            );
            itemResponses.add(orderItemResponse);
        }

        OrderResponse orderResponse = new OrderResponse(
                savedOrder.getOrderid(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                itemResponses
        );
        return orderResponse;
    }

    /**
     * Update product information in vector store
     */
    private void updateProductInVectorStore(product p) {
        try {
            // Delete old product entry
            String filter = String.format("product_id == '%s'", String.valueOf(p.getId()));
            vectorStore.delete(filter);

            // Create updated content with all product information
            String updatedContent = String.format(
                    "Product: %s, Brand: %s, Category: %s, Description: %s, Price: %.2f, Stock: %d, Available: %s",
                    p.getProductName(),
                    p.getBrand() != null ? p.getBrand() : "",
                    p.getCategory() != null ? p.getCategory() : "",
                    p.getDescription() != null ? p.getDescription() : "",
                    p.getPrice(),
                    p.getStockQuantity(),
                    p.isProductAvailable()
            );

            Document updatedDoc = new Document(
                    updatedContent,
                    Map.of(
                            "type", "product",
                            "product_id", String.valueOf(p.getId()),
                            "name", p.getProductName(),
                            "brand", p.getBrand() != null ? p.getBrand() : "",
                            "category", p.getCategory() != null ? p.getCategory() : "",
                            "price", String.valueOf(p.getPrice())
                    )
            );

            vectorStore.add(List.of(updatedDoc));
        } catch (Exception e) {
            System.err.println("Failed to update product in vector store: " + e.getMessage());
        }
    }

    /**
     * Add order to vector store for chatbot retrieval
     */
    private void addOrderToVectorStore(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("Order ID: ").append(order.getOrderid()).append("\n");
        content.append("Customer: ").append(order.getCustomerName()).append("\n");
        content.append("Email: ").append(order.getEmail()).append("\n");
        content.append("Order Date: ").append(order.getOrderDate()).append("\n");
        content.append("Status: ").append(order.getStatus()).append("\n");
        content.append("Products ordered:\n");

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderitems()) {
            content.append("- ").append(item.getP().getProductName())
                    .append(" (Qty: ").append(item.getQuantity())
                    .append(", Price: $").append(item.getTotalPrice()).append(")\n");
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        content.append("Total Amount: $").append(totalAmount);

        Document document = new Document(
                content.toString(),
                Map.of(
                        "type", "order",
                        "orderId", order.getOrderid(),
                        "customerName", order.getCustomerName(),
                        "email", order.getEmail(),
                        "status", order.getStatus()
                )
        );
        vectorStore.add(List.of(document));
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersResponse() {
        // Use custom query with eager loading to prevent LazyInitializationException
        List<Order> orders = or.findAllWithItems();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (OrderItem item : order.getOrderitems()) {
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getP().getProductName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                itemResponses.add(orderItemResponse);
            }
            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderid(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
            );
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }
    /**
     * Get order by order ID with all details
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        Order order = or.findByOrderidWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getOrderitems()) {
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getP().getProductName(),
                    item.getQuantity(),
                    item.getTotalPrice()
            );
            itemResponses.add(orderItemResponse);
        }
        return new OrderResponse(
                order.getOrderid(),
                order.getCustomerName(),
                order.getEmail(),
                order.getStatus(),
                order.getOrderDate(),
                itemResponses
        );
    }

    /**
     * Get orders by customer email
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByEmail(String email) {
        List<Order> orders = or.findByEmailWithItems(email);
        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (OrderItem item : order.getOrderitems()) {
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getP().getProductName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                itemResponses.add(orderItemResponse);
            }
            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderid(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
            );
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }
}