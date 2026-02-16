package com.yolo.productSite.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityScan
@Builder
public class product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String productName;
    private int price;
    private Integer stockQuantity;
    private String category;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date releaseDate;
    private boolean productAvailable;
    @Lob
    @Column(columnDefinition= "TEXT")
    private String description;
    private String imageName;
    private String imageType;
    private String brand;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    public product(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductName() { return productName; }
    public int getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getDescription() { return description; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public Date getReleaseDate() { return releaseDate; }
    public boolean isProductAvailable() { return productAvailable; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public byte[] getImageData() { return imageData; }
    public String getImageType() { return imageType; }
}