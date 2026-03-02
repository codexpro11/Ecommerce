package com.yolo.productSite;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorConfig {
    private static final int VECTOR_SIZE = 768;

    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                "4dabe06b-d700-4f00-85d3-684153d3476b.europe-west3-0.gcp.cloud.qdrant.io",
                6334,   // gRPC port (Spring AI uses this)
                true).withApiKey("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3MiOiJtIn0.wgHEcnLn2csBb4tTriVCP-Eo0yfPiwuKaBXtK7_clN0");
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