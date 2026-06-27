package com.yolo.productSite.controller;

import com.yolo.productSite.model.product;
import com.yolo.productSite.service.ProductService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class SearchController
{
    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }
    // Endpoint to add product (Syncs MySQL + Qdrant)
    @PostMapping
    public String addProduct(@RequestBody product p) {
        productService.addProduct(p);
        return "Product added to MySQL and Qdrant!";
    }

    // Endpoint to search (Queries Qdrant)
    @GetMapping("/search-results")
    public List<product> search(@RequestParam String query) {
        return productService.SyncAllProductsToVectorStore();
    }
    @PostMapping("/sync")
    public String sync()
    {
        productService.SyncAllProductsToVectorStore();
        return "all product synced to qdrant";
    }
}
