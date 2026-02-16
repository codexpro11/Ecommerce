package com.yolo.productSite;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class VectorConfig {
 @Bean
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                "localhost",
                6334,
                false);
        return new QdrantClient(grpcClientBuilder.build());
    }
@Bean
    public VectorStore customvectorStore(QdrantClient qdrantClient,EmbeddingModel embeddingModel)
{
    return QdrantVectorStore
            .builder(qdrantClient,embeddingModel)
            .collectionName("vector_store")
            .initializeSchema(true)
            .build();
}
    }
