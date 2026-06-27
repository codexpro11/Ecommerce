package com.yolo.productSite.service;

import com.yolo.productSite.Productrepo.Productrepo;
import com.yolo.productSite.model.product;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private final Productrepo productRepo;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AiImageService aiImageService;

    private VectorStore vectorStore;

    @Autowired
    public ProductService(Productrepo productRepo, ChatClient chatClient, AiImageService aiImageService, VectorStore vectorStore) {
        this.productRepo = productRepo;
        this.chatClient = chatClient;
        this.aiImageService = aiImageService;
        this.vectorStore = vectorStore;
    }

    public List<product> getAllProducts() {
        return productRepo.findAll();
    }

    public product getProductById(int id) {
        return productRepo.findById(id).orElse(new product(-1));
    }

    public product addOrUpdateProduct(product p, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            p.setImageName(image.getOriginalFilename());
            p.setImageType(image.getContentType());
            p.setImageData(image.getBytes());
        }
        return productRepo.save(p);
    }

    public void deleteProduct(int id) {
        productRepo.deleteById(id);
    }

    public List<product> findByProductNameContaining(String keyword) {
        return productRepo.findByproductNameContaining(keyword);
    }

    // ai for description
    public String Description(String productName, String category) {
        String prompt = String.format("""
                Give a short and engaging product description for an e-commerce website.
                Product Name: %s
                Category: %s
                """, productName, category);
        String response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return response;
    }

    public byte[] generateImage(String productName, String category, String brand) {
        String ImagePrompt = String.format("""
                provide an image for ecommerce site considering following parameter
                productName: %s
                brand: %s
                category: %s
               
                """, productName, category, brand);
        byte[] aiImage = aiImageService.generateImage(ImagePrompt);
        return aiImage;
    }

    public product generateProduct(String query) {
        String prompt = String.format("""
                Generate a product based on this description: %s
                Return ONLY the JSON matching this structure:
                {
                    "productName": "String",
                    "brand": "String",
                    "description": "String",
                    "price": number,
                    "category": "String",
                    "stockQuantity": number,
                    "releaseDate": "YYYY-MM-DD",
                    "productAvailable": boolean
                }
                """, query);
        return chatClient
                .prompt(prompt)
                .user(query)
                .call()
                .entity(product.class);
    }

    @Transactional
    public void addProduct(product p) {
        // 1. Save to MySQL (Source of Truth)
        product savedProduct = productRepo.save(p);

        // 2. Prepare data for Qdrant (Search Index)
        // Create a rich text representation for better embeddings
        String contentToEmbed = String.format(
                "Product: %s, Brand: %s, Category: %s, Description: %s, Price: %.2f",
                savedProduct.getProductName(),
                savedProduct.getBrand() != null ? savedProduct.getBrand() : "",
                savedProduct.getCategory() != null ? savedProduct.getCategory() : "",
                savedProduct.getDescription() != null ? savedProduct.getDescription() : "",
                (double) savedProduct.getPrice()
        );

        Document document = new Document(
                contentToEmbed,
                Map.of(
                        "type", "product",
                        "product_id", String.valueOf(savedProduct.getId()),
                        "name", savedProduct.getProductName(),
                        "brand", savedProduct.getBrand() != null ? savedProduct.getBrand() : "",
                        "category", savedProduct.getCategory() != null ? savedProduct.getCategory() : "",
                        "price", String.valueOf(savedProduct.getPrice())
                )
        );

        // 3. Save to Qdrant
        vectorStore.add(List.of(document));
    }
    /**
     * Search for products using vector similarity
     * @param query Natural language search query
     * @return List of matching products
     */
    public List<product> searchProducts(String query) {
        // Use SearchRequest for more control over the search
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(10)// Return top 5 most similar results
                .similarityThreshold(0.1f)
                .build(); // Only return results with similarity > 0.5

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        // Convert Documents back to product objects
        return documents.stream()
                .map(doc -> {
                    String productIdStr = (String) doc.getMetadata().get("product_id");
                    if (productIdStr != null) {
                        try {
                            int productId = Integer.parseInt(productIdStr);
                            return productRepo.findById(productId).orElse(null);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    // Auto-sync all products into Qdrant on every app startup
    @PostConstruct
    public void syncOnStartup() {
        try {
            System.out.println(">>> [ProductService] Syncing all products to vector store on startup...");
            List<product> synced = SyncAllProductsToVectorStore();
            System.out.println(">>> [ProductService] Successfully indexed " + synced.size() + " products into Qdrant.");
        } catch (Exception e) {
            System.err.println(">>> [ProductService] Failed to sync products to vector store: " + e.getMessage());
        }
    }

    // method to sync existing products to qdrant database
    @Transactional
    public List<product> SyncAllProductsToVectorStore()
    {
        List<product> allProducts=productRepo.findAll();
        List<Document> documents = allProducts.stream().map(p -> {
            String contentToEmbed = String.format(
                    "Product: %s, Brand: %s, Category: %s, Description: %s, Price: %.2f, Stock: %d, Available: %s",
                    p.getProductName(),
                    p.getBrand() != null ? p.getBrand() : "",
                    p.getCategory() != null ? p.getCategory() : "",
                    p.getDescription() != null ? p.getDescription() : "",
                    (double) p.getPrice(),
                    p.getStockQuantity(),
                    p.isProductAvailable() ? "Yes" : "No");
            return new Document(contentToEmbed, Map.of(
                    "type", "product",
                    "product_id", String.valueOf(p.getId()),
                    "name", p.getProductName(),
                    "brand", p.getBrand() != null ? p.getBrand() : "",
                    "category", p.getCategory() != null ? p.getCategory() : "",
                    "price", String.valueOf(p.getPrice())
            ));
        }).collect(Collectors.toList());
        vectorStore.add(documents);
        return allProducts;
    }
    /**
     * Enhanced search that combines vector search with keyword search
     * @param query Search query
     * @return Combined results from both vector and keyword search
     */
    public List<product> enhancedSearch(String query) {
        // Try vector search first
        List<product> vectorResults = searchProducts(query);

        // If vector search returns results, use them
        if (!vectorResults.isEmpty()) {
            return vectorResults;
        }
        // Fallback to keyword search if vector search finds nothing
        return findByProductNameContaining(query);
    }
    /**
     * Get product suggestions for chatbot
     * @param userQuery User's natural language query
     * @return Formatted string with product suggestions
     */
    public String getProductSuggestions(String userQuery) {
        List<product> products = enhancedSearch(userQuery);

        if (products.isEmpty()) {
            return "I couldn't find any products matching your query. Please try different keywords.";
        }

        StringBuilder suggestions = new StringBuilder();
        suggestions.append("Here are some products I found:\n\n");
        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            product p = products.get(i);
            suggestions.append(String.format("%d. %s - %s\n",
                    i + 1,
                    p.getProductName(),
                    p.getBrand() != null ? p.getBrand() : ""));

            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                suggestions.append("   Description: ")
                        .append(p.getDescription().substring(0, Math.min(100, p.getDescription().length())))
                        .append("...\n");
            }

            suggestions.append(String.format("   Price: $%.2f\n", p.getPrice()));
            suggestions.append(String.format("   In Stock: %s\n\n",
                    p.getStockQuantity() > 0 ? "Yes" : "No"));
        }

        return suggestions.toString();
    }

    public List<product> addProductJson(List<product> products) {
        List<product> savedProducts = productRepo.saveAll(products);

        // Also add to vector store
        List<Document> documents = savedProducts.stream()
                .map(p -> {
                    String contentToEmbed = String.format(
                            "Product: %s, Brand: %s, Category: %s, Description: %s, Price: %.2f",
                            p.getProductName(),
                            p.getBrand() != null ? p.getBrand() : "",
                            p.getCategory() != null ? p.getCategory() : "",
                            p.getDescription() != null ? p.getDescription() : "",
                            (double)p.getPrice()
                    );

                    return new Document(
                            contentToEmbed,
                            Map.of(
                                    "type", "product",
                                    "product_id", String.valueOf(p.getId()),
                                    "name", p.getProductName(),
                                    "brand", p.getBrand() != null ? p.getBrand() : "",
                                    "category", p.getCategory() != null ? p.getCategory() : "",
                                    "price", String.valueOf(p.getPrice())
                            )
                    );
                })
                .collect(Collectors.toList());

        vectorStore.add(documents);

        return savedProducts;
    }

    // bulk image upload
    public void uploadImagesBulk(Map<String, MultipartFile> files) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        files.forEach((key, file) -> {
            executor.submit(() -> {
                try {
                    int productId = Integer.parseInt(key.replace("image_", ""));
                    product p = productRepo.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"));

                    p.setImageName(file.getOriginalFilename());
                    p.setImageType(file.getContentType());
                    p.setImageData(file.getBytes());

                    productRepo.save(p);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        executor.shutdown();
    }
}