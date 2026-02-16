package com.yolo.productSite.controller;

import com.yolo.productSite.service.ChatService;
import org.hibernate.dialect.unique.CreateTableUniqueDelegate;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {


    @Autowired
    private ChatService chatService;

    @GetMapping("/ask")
    public ResponseEntity<String> ChatBot(@RequestParam String message)
    {
        String Response = chatService.getBotResponse(message);

        return ResponseEntity.ok(Response);

    }
}
