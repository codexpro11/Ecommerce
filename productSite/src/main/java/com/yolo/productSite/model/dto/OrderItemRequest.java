package com.yolo.productSite.model.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderItemRequest(
      @JsonProperty("productId") int productId,
        @JsonProperty("quantity") int quantity
) {
    @JsonCreator
    public OrderItemRequest {}
}
