package com.yolo.productSite.controller;

import com.yolo.productSite.Productrepo.WishListRepo;
import com.yolo.productSite.model.WishList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
public class WishListController {

    private final WishListRepo wishListRepo;

    public WishListController(WishListRepo wishListRepo) {
        this.wishListRepo = wishListRepo;
    }

    // ✅ SMART TOGGLE: Handles both Add and Remove
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleWishlist(@RequestBody WishList item) {

        Optional<WishList> existingItem = wishListRepo.findByProductId(item.getProductId());

        if (existingItem.isPresent()) {
            wishListRepo.delete(existingItem.get());
            return ResponseEntity.ok("removed");
        }

        WishList newItem = new WishList();
        newItem.setProductId(item.getProductId());
        newItem.setProductName(item.getProductName());

        wishListRepo.save(newItem);

        return ResponseEntity.ok("added");
    }

    // Keep this to load the wishlist page
    @GetMapping("/")
    public List<WishList> getAllWishlist() {
        return wishListRepo.findAll();
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> deleteWishlist(@PathVariable String id) {
        wishListRepo.deleteById(Long.valueOf(id));
        return ResponseEntity.ok("Deleted");
    }

    // Check if a product is in wishlist
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkWishlist(@PathVariable int productId) {
        Optional<WishList> item = wishListRepo.findByProductId(productId);
        return ResponseEntity.ok(item.isPresent());
    }
}