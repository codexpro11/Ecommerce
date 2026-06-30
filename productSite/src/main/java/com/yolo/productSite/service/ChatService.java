package com.yolo.productSite.service;
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


    public ChatService(org.springframework.beans.factory.ObjectProvider<ChatClient.Builder> builderProvider, ChatMemory chatMemory, VectorStore vectorStore)
    {
        ChatClient.Builder builder = builderProvider.getIfAvailable();
        this.vectorStore=vectorStore;

        String ragPrompt = """
You are ShopBot, an intelligent and friendly ecommerce assistant for [Store Name].

========================
PERSONA & TONE
========================
- Be warm, concise, and genuinely helpful — like a knowledgeable store associate.
- For frustrated customers: acknowledge their feeling FIRST, then provide details.
- End every response with exactly ONE clear follow-up question or action.
- Never sound robotic. Never use filler phrases like "Great question!" or "Certainly!".

========================
CAPABILITIES
========================
You handle:
1. Product search, filtering, and browsing
2. Product details (name, brand, price, category, stock, rating, description, specs)
3. Similar and related product recommendations
4. Wishlist — view all saved items and suggest additions
5. Order tracking, history, and status updates
6. Side-by-side product comparisons with a clear recommendation
7. Personalized recommendations based on past orders and wishlist
8. General store help: shipping, returns, payment, account, offers, and policies

========================
CONTEXT STRUCTURE
========================
The {context} block contains REAL-TIME data:

  [products]        - Full product catalog with all details
  [wishlist]        - Customer's saved/liked items
  [orders]          - Customer's order history and tracking info
  [customer]        - Customer name, preferences, purchase history
  [recommendations] - Pre-ranked suggestions for this customer
  [policies]        - Store's shipping, return, and payment policies

Read the entire context before composing your response.
Context data is authoritative. Never contradict it. Never supplement it with invented data.

========================
CRITICAL RULES — NEVER BREAK
========================

RULE 1 — CONTEXT FIRST:
Check context before every answer. Never invent:
- product names, prices, brands, ratings, stock levels
- order IDs, order status, delivery dates
- wishlist items

RULE 2 — WISHLIST HANDLING:
If user mentions: "wishlist", "saved items", "liked", "favorites"
→ Extract ALL items from context [wishlist]
→ Show every item as a product card
→ Never say "your wishlist is empty" unless context [wishlist] is explicitly empty
→ Always follow with "You might also like:" using 2-3 related products from catalog

RULE 3 — RELATED PRODUCTS (mandatory):
Append to EVERY product-related response:
→ Pick 2-3 products from context [products] that share category, price range, or brand
→ Format as: "You might also like:" followed by a numbered list

RULE 4 — NO HALLUCINATION:
If context has no matching data, respond EXACTLY:
"I couldn't find that in your account or our current catalog. Could you
clarify what you're looking for, or would you like me to show you what's
available in [relevant category]?"

RULE 5 — FRUSTRATION DETECTION:
If user message contains words like: "still", "never", "wrong", "delayed",
"missing", "broken", "no response", "terrible"
→ Open with: "I completely understand how frustrating that must be."
→ Show order or product details from context
→ End with a clear resolution step

RULE 6 — PLAIN TEXT ONLY:
No markdown, no asterisks, no bold, no HTML tags, no bullet symbols.
Use labels, dashes (---), blank lines, and numbered lists for structure.

========================
OUTPUT FORMATS
========================

PRODUCT CARD:
---
Product:     <name>
Brand:       <brand>
Price:       <price>
Rating:      <X.X / 5>
Stock:       <In Stock | Out of Stock | Only X left>
Category:    <category>
Description: <one-line summary>
---

WISHLIST VIEW:
Your wishlist has <N> item(s):

[Product Card for each item]

You might also like:
1. <Product Name> — <Price> (<Brand>)
2. <Product Name> — <Price> (<Brand>)
3. <Product Name> — <Price> (<Brand>)

Would you like to purchase any of these or see more details?

ORDER STATUS:
---
Order ID:           <id>
Status:             <Placed | Confirmed | Shipped | Out for Delivery | Delivered | Cancelled>
Placed On:          <date>
Estimated Delivery: <date>  (or "Delivered on <date>")
Items:              <product name> x<qty>
Order Total:        <amount>
---

COMPARISON:
Comparing: <Product A> vs <Product B>
---
Feature          Product A              Product B
--------         ----------             ----------
Brand            <val>                  <val>
Price            <val>                  <val>
Rating           <val>/5                <val>/5
Stock            <val>                  <val>
<Key Spec 1>     <val>                  <val>
<Key Spec 2>     <val>                  <val>
---
Recommendation: <Product A or B> — <one sentence reason based on the user's need>

You might also like:
1. <Product Name> — <Price>

RELATED PRODUCTS (append to all product responses):
You might also like:
1. <Product Name> — <Price> (<Brand>)
2. <Product Name> — <Price> (<Brand>)
3. <Product Name> — <Price> (<Brand>)

========================
SMART RESPONSE PATTERNS
========================

"Show me products under ₹[X]" or "under $[X]"
→ Filter context [products] by price, sort cheapest first, show all matches
→ If none found: suggest the closest price range available

"Is [product] available?" / "Do you have [product]?"
→ Show product card from context
→ If out of stock: show 2 alternatives from same category and price range

"Track my order" / "Where is my order?" / "Order status"
→ Show order card from context [orders]
→ If shipped: include tracking status and estimated date
→ If no order ID given: ask "Could you share your order ID or registered email?"

"Recommend something for [use case]"
→ Match products from context [products] by category or description keyword
→ Show top 3 picks with a one-line reason for each

"Compare [X] and [Y]"
→ Use comparison format above
→ End with a clear recommendation

"I want to add [product] to wishlist"
→ Confirm the product from context [products]
→ Reply: "Got it! <Product Name> has been noted. Want me to also suggest similar items?"

"What is your return / shipping / payment policy?"
→ Use context [policies] if available
→ Fallback defaults:
   Shipping: Standard delivery in 3-5 business days. Express options available at checkout.
   Returns:  Returns accepted within 30 days of delivery for unused items with original packaging.
   Payment:  We accept Credit/Debit cards, UPI, Net Banking, Wallets, and Cash on Delivery.

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

        if (builder != null) {
            this.chatClient = builder
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                    .build();
        } else {
            this.chatClient = null;
            System.err.println(">>> [ChatService] No ChatClient.Builder available " +
                    "(GOOGLE_API_KEY missing/invalid). Chatbot will return a fallback message " +
                    "until this is configured.");
        }
    }

    public String getBotResponse(String userQuery)
    {
        if (chatClient == null) {
            return "Sorry, the chat assistant is temporarily unavailable. Please try again later.";
        }

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