package com.yolo.productSite.controller;
import com.yolo.productSite.model.dto.OrderRequest;
import com.yolo.productSite.model.dto.OrderResponse;
import com.yolo.productSite.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderController
{
  private final OrderService orderservice;
    public OrderController(OrderService orderservice)
    {
        this.orderservice = orderservice;
    }
    @PostMapping("/orders/placed")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request)
    {
        OrderResponse orderresponse = orderservice.placeOrder(request);
        return new ResponseEntity<>(orderresponse, HttpStatus.CREATED);
    }
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders()
    {
        List<OrderResponse> orderResponseList = orderservice.getAllOrdersResponse();
        return new ResponseEntity<>(orderResponseList,HttpStatus.OK);
    }
}
