package com.yolo.productSite;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductSiteApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        ChatClient chatClient() {
            return Mockito.mock(ChatClient.class);
        }

        @Bean
        @Primary
        ChatClient.Builder chatClientBuilder(ChatClient chatClient) {
            ChatClient.Builder builder = Mockito.mock(ChatClient.Builder.class, Mockito.RETURNS_SELF);
            Mockito.when(builder.build()).thenReturn(chatClient);
            return builder;
        }

        @Bean
        @Primary
        @Qualifier("googleGenAiChatModel")
        ChatModel chatModel() {
            return Mockito.mock(ChatModel.class);
        }

        @Bean
        @Primary
        ChatMemory chatMemory() {
            return Mockito.mock(ChatMemory.class);
        }

        @Bean
        @Primary
        VectorStore vectorStore() {
            return Mockito.mock(VectorStore.class);
        }

        @Bean
        @Primary
        EmbeddingModel embeddingModel() {
            return Mockito.mock(EmbeddingModel.class);
        }

        @Bean
        @Primary
        ImageModel imageModel() {
            return Mockito.mock(ImageModel.class);
        }

        @Bean
        @Primary
        QdrantClient qdrantClient() {
            return Mockito.mock(QdrantClient.class);
        }
    }
}
