package com.yolo.productSite.controller;

import com.yolo.productSite.model.product;
import com.yolo.productSite.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.Document;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class productcontroller {

    private final ProductService productService;

    @Autowired
    public productcontroller(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("product") product product,
            @RequestPart(value = "imageFile", required = false) MultipartFile image) {
        try {
            product saved = productService.addOrUpdateProduct(product, image);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<product> getProductById(@PathVariable int id) {
        product product = productService.getProductById(id);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/product/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        product p = productService.getProductById(id);

        if (p == null || p.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(p.getImageType()))
                .body(p.getImageData());
    }

    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<product> updateProduct(
            @PathVariable int id,
            @RequestPart("product") product p,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        p.setId(id);
        product updated = productService.addOrUpdateProduct(p, imageFile);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        product existingProduct = productService.getProductById(id);

        if (existingProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/product/search")
    public ResponseEntity<List<product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(
                productService.findByProductNameContaining(keyword));
    }
    // adding product in json format
    @PostMapping
    public ResponseEntity<List<product>> Json(@RequestBody List<product> products)
    {
        List<product> savedProducts = productService.addProductJson(products);
        return new ResponseEntity<>(savedProducts,HttpStatus.OK);
    }


    @PostMapping("/product/generate-description")
    public ResponseEntity<String> generateDescription(@RequestParam String productName, @RequestParam String category) {
        try {
            String AIDesc = productService.Description(productName, category);
            return new ResponseEntity<>(AIDesc, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/product/generate-image")
    public ResponseEntity<?> generateImage(@RequestParam String productName, @RequestParam String description,
            @RequestParam String category) {
        try {
            byte[] aiImage = productService.generateImage(productName, category, description);
            return new ResponseEntity<>(aiImage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/product/generate-product")
    public ResponseEntity<product> generateProduct(@RequestParam String query) {
        try {
            product p = productService.generateProduct(query);
            return new ResponseEntity<>(p, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //bulk image upload
    @PostMapping(
            value = "/products/images/bulk",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadImagesBulk(
            @RequestParam Map<String, MultipartFile> files) {

        productService.uploadImagesBulk(files);
        return ResponseEntity.ok("Images uploaded successfully");
    }
}
