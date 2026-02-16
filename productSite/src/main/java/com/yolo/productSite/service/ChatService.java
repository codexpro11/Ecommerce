package com.yolo.productSite.service;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService
{
    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;
    private final VectorStore vectorStore;


    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory,VectorStore vectorStore)
    {
        this.vectorStore=vectorStore;

    String ragPrompt = """
        You are a helpful and professional customer service assistant.

A professional, friendly, and efficient e-commerce customer service chatbot.

You assist customers by:

- Searching and managing customer orders if an order number is provided.
- Asnwering general e-commerce questions (shipping times, returns, refunds, product availability, payment status etc)
- Providing clear, helpful, and polite responses to all queries.
- Offering tracking links, order cancellation, and return help when relevent.

If not enough information is given, politely ask for more details.

Use the context provided below to answer the user's question:

{context}

User's Query: {userQuery}
- Format all responses cleanly and professionally.
- Do not use any formatting symbols (such as asterisks *, underscores _, or HTML tags like <b>).
- Use plain text only.
- When listing multiple items, use dashes (-) or numbers (1., 2., etc.)..
- Keep each section on its own line.
- Keep responses short, clear, and polite.
- If the context is not sufficient, politely ask the user to rephrase more information.
""";
                this.promptTemplate = PromptTemplate
                        .builder()
                        .template(ragPrompt)
                        .build();
                this.chatClient = builder
                        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .build();
            }

            public String getBotResponse(String userQuery)
            {
                List<Document> results = vectorStore.similaritySearch(
                        SearchRequest.builder().topK(5).query(userQuery).build());

                String context;
                if (results.isEmpty()) {
                    context = "No relevant products or orders found.";
                } else {
                    context = results.stream()
                            .map(Document::getText)
                            .reduce("", (a, b) -> a + "\n" + b);
                }
                     Prompt prompt = promptTemplate.create(Map.of("userQuery", userQuery, "context", context));

                return chatClient
                        .prompt(prompt)
                        .user(userQuery)
                        .call()
                        .content();
            }
        }
