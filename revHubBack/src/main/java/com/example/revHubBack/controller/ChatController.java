package com.example.revHubBack.controller;

import com.example.revHubBack.entity.ChatMessage;
import com.example.revHubBack.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String receiverUsername = request.get("receiverUsername");
            String content = request.get("content");
            
            ChatMessage message = chatService.sendMessage(authentication.getName(), receiverUsername, content);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/conversation/{username}")
    public ResponseEntity<List<ChatMessage>> getConversation(@PathVariable String username, Authentication authentication) {
        try {
            List<ChatMessage> messages = chatService.getConversation(authentication.getName(), username);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/mark-read/{username}")
    public ResponseEntity<String> markAsRead(@PathVariable String username, Authentication authentication) {
        try {
            chatService.markMessagesAsRead(authentication.getName(), username);
            return ResponseEntity.ok("Messages marked as read");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/contacts")
    public ResponseEntity<List<String>> getChatContacts(Authentication authentication) {
        try {
            List<String> contacts = chatService.getChatContacts(authentication.getName());
            return ResponseEntity.ok(contacts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/unread-count/{username}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String username, Authentication authentication) {
        try {
            long count = chatService.getUnreadMessageCount(authentication.getName(), username);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(0L);
        }
    }
}