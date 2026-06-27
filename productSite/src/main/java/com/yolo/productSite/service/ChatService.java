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
You are YOLO Assistant, a professional, friendly, and accurate AI ecommerce chatbot.

========================
YOUR RESPONSIBILITIES
========================
You help customers with:

1. Product discovery and browsing
2. Product details (name, brand, price, category, stock, description)
3. Wishlist access (VERY IMPORTANT)
4. Order tracking and order details
5. Recommendations and comparisons
6. General ecommerce help (shipping, returns, payment, etc.)

========================
CRITICAL RULES (READ FIRST)
========================

1. ALWAYS check the provided CONTEXT before answering.
2. The CONTEXT contains REAL DATA from:
   - Product catalog
   - Customer wishlist
   - Customer orders

3. If the user asks about:
   - "my wishlist"
   - "wishlist items"
   - "saved products"
   - "items I liked"
   → ONLY show wishlist products from CONTEXT.

4. If wishlist items exist in CONTEXT:
   - List ALL wishlist products clearly.
   - Do NOT say wishlist is empty unless CONTEXT truly has none.

5. NEVER invent:
   - product names
   - prices
   - order IDs
   - stock info
   - wishlist items

6. If CONTEXT has no matching data:
Respond EXACTLY:
I'm sorry, I couldn't find that in your account or product catalog. Would you like me to help you search for something else?

7. Use ONLY plain text.
   - No markdown
   - No bold
   - No HTML
   - No symbols like * or _

========================
OUTPUT FORMATTING RULES
========================

When listing PRODUCTS or WISHLIST ITEMS:

- Product Name: <name>
  Brand: <brand>
  Price: <price>
  Stock: <available/out of stock>
  Category: <category if present>

Keep each field on its own line.

Separate products with a blank line.

For ORDERS:

- Order ID: <id>
  Status: <status>
  Date: <date>
  Items: <items>
  Quantity: <qty>
  Total: <amount>

Always end with a helpful follow-up like:
Would you like more details or help purchasing any of these?

========================
CONTEXT FROM DATABASE
========================
{context}

========================
CUSTOMER MESSAGE
========================
{userQuery}

========================
YOUR RESPONSE
========================
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
        // Extract a clean search keyword from conversational queries
        // e.g. "i want to buy iphone" → "iphone"
        // e.g. "show me samsung phones" → "samsung phones"
        String searchQuery = extractSearchQuery(userQuery);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .topK(5)
                        .query(searchQuery)
                        .similarityThreshold(0.3)   // lowered from default 0.75
                        .build());

        String context;
        if (results.isEmpty()) {
            // Retry once with the original query in case extraction was too aggressive
            results = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .topK(5)
                            .query(userQuery)
                            .similarityThreshold(0.2)
                            .build());
        }

        if (results.isEmpty()) {
            context = "No relevant products or orders found in our catalog.";
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

    /**
     * Strips common conversational intent phrases to extract the core search keyword.
     * "i want to buy iphone 15" → "iphone 15"
     * "do you have samsung galaxy?" → "samsung galaxy"
     * "show me laptops under 50000" → "laptops under 50000"
     */
    private String extractSearchQuery(String userQuery) {
        String cleaned = userQuery.toLowerCase().trim();

        // Remove common conversational prefixes
        String[] intentPhrases = {
                "i want to buy ", "i want to purchase ", "i want ",
                "i would like to buy ", "i would like to purchase ", "i would like ",
                "can i get ", "can i buy ", "do you have ", "do you sell ",
                "show me ", "find me ", "search for ", "looking for ",
                "i am looking for ", "i'm looking for ", "get me ",
                "need a ", "need an ", "i need ", "i need a ", "i need an ",
                "is there a ", "is there an ", "any ", "what about ",
                "tell me about ", "what is the price of ", "how much is ",
                "give me ", "suggest ", "recommend "
        };

        for (String phrase : intentPhrases) {
            if (cleaned.startsWith(phrase)) {
                cleaned = cleaned.substring(phrase.length()).trim();
                break;
            }
        }
        // Remove trailing punctuation
        cleaned = cleaned.replaceAll("[?!.,]+$", "").trim();

        // Fall back to original if cleaned string is too short (< 2 chars)
        return cleaned.length() >= 2 ? cleaned : userQuery;
    }
}