package com.yolo.productSite;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AI chat configuration.
 *
 * Spring AI's own ChatClientAutoConfiguration requires a ChatModel bean
 * UNCONDITIONALLY — if GOOGLE_API_KEY is missing or invalid, that
 * auto-configuration throws and takes down the entire application context
 * on startup (see: ProductService, OrderService, WishList, everything,
 * even though none of them need AI to function).
 *
 * We exclude that auto-configuration (see application.properties:
 * spring.autoconfigure.exclude) and build the equivalent beans ourselves
 * here, but tolerate a missing ChatModel: if none is available, chatClient
 * / chatClientBuilder simply resolve to null (Spring registers this as a
 * "NullBean", so required @Autowired injection points still succeed — they
 * just receive null). Callers (ProductService, ChatService) check for null
 * and degrade gracefully instead of crashing.
 */
@Configuration
@Profile("!test")
public class AI_config
{
    @Bean
    public ChatClient chatClient(ObjectProvider<ChatModel> chatModelProvider)
    {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            System.err.println(">>> [AI_config] No ChatModel bean available " +
                    "(GOOGLE_API_KEY missing/invalid). AI description generation disabled.");
            return null;
        }
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ObjectProvider<ChatModel> chatModelProvider)
    {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            System.err.println(">>> [AI_config] No ChatModel bean available " +
                    "(GOOGLE_API_KEY missing/invalid). Chatbot disabled.");
            return null;
        }
        return ChatClient.builder(chatModel);
    }
}