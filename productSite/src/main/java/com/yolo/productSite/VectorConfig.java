package com.yolo.productSite;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile("!test")
public class VectorConfig {

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port:6334}")
    private int port;

    @Value("${spring.ai.vectorstore.qdrant.use-tls:true}")
    private boolean useTls;

    @Value("${spring.ai.vectorstore.qdrant.api-key:}")
    private String apiKey;

    @Value("${spring.ai.vectorstore.qdrant.url:}")
    private String url;

    @Bean
    public QdrantClient qdrantClient() {
        String finalHost = host;
        int finalPort = port;

        // If a URL was provided (e.g. QDRANT_URL in Render) but HOST was not, extract the host from the URL.
        if (StringUtils.hasText(url) && "localhost".equals(host)) {
            String parsedUrl = url.replace("https://", "").replace("http://", "");
            if (parsedUrl.contains(":")) {
                String[] parts = parsedUrl.split(":");
                finalHost = parts[0];
                try {
                    finalPort = Integer.parseInt(parts[1]);
                } catch(NumberFormatException ignored) {}
            } else {
                finalHost = parsedUrl;
            }
        }

        System.out.println(">>> [VectorConfig] Connecting to Qdrant at " + finalHost + ":" + finalPort + " (TLS=" + useTls + ")");

        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                finalHost,
                finalPort,
                useTls);
        
        if (StringUtils.hasText(apiKey)) {
            grpcClientBuilder.withApiKey(apiKey);
        }
        
        return new QdrantClient(grpcClientBuilder.build());
    }

    @Bean
    public VectorStore customvectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        return QdrantVectorStore
                .builder(qdrantClient, embeddingModel)
                .collectionName("vector_store")
                .initializeSchema(true)   // auto-creates collection if not exists
                .build();
    }
}
