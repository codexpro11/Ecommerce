package com.yolo.productSite;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AI_config
{
    @Bean
    public ChatClient chatClient (@Qualifier("googleGenAiChatModel") ChatModel chatModel)
    {
        return ChatClient.builder(chatModel).build();
    }

}
