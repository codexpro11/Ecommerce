package com.yolo.productSite;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class VectorConfig {
    private static final int VECTOR_SIZE = 768;

    @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                "qdrant.railway.internal",
                6334,   // gRPC port (Spring AI uses this)
                false);
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
