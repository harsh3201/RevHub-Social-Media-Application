package com.example.revHubBack.service;

import com.example.revHubBack.entity.ChatMessage;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.ChatMessageRepository;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.service.NotificationMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMongoService notificationService;
    
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        ChatMessage message = new ChatMessage();
        message.setSenderId(sender.getId().toString());
        message.setSenderUsername(sender.getUsername());
        message.setReceiverId(receiver.getId().toString());
        message.setReceiverUsername(receiver.getUsername());
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        createMessageNotification(receiver, sender, content);
        
        return savedMessage;
    }
    
    public List<ChatMessage> getConversation(String username1, String username2) {
        try {
            User user1 = userRepository.findByUsername(username1)
                    .orElseThrow(() -> new RuntimeException("User1 not found"));
            
            User user2 = userRepository.findByUsername(username2)
                    .orElseThrow(() -> new RuntimeException("User2 not found"));
            
            return chatMessageRepository.findConversation(user1.getId().toString(), user2.getId().toString());
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public void markMessagesAsRead(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        List<ChatMessage> unreadMessages = chatMessageRepository.findConversation(
                sender.getId().toString(), receiver.getId().toString());
        
        unreadMessages.stream()
                .filter(msg -> msg.getReceiverId().equals(receiver.getId().toString()) && !msg.isRead())
                .forEach(msg -> {
                    msg.setRead(true);
                    chatMessageRepository.save(msg);
                });
    }
    
    public List<String> getChatContacts(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get all messages involving this user
            List<ChatMessage> messages = chatMessageRepository.findAllUserChats(user.getId().toString());
            
            // Extract unique contact usernames
            java.util.Set<String> contactSet = new java.util.HashSet<>();
            for (ChatMessage msg : messages) {
                if (!msg.getSenderUsername().equals(username)) {
                    contactSet.add(msg.getSenderUsername());
                }
                if (!msg.getReceiverUsername().equals(username)) {
                    contactSet.add(msg.getReceiverUsername());
                }
            }
            
            return new java.util.ArrayList<>(contactSet);
        } catch (Exception e) {
            System.out.println("Error getting chat contacts: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    private void createMessageNotification(User receiver, User sender, String content) {
        notificationService.createMessageNotification(receiver, sender, content);
    }
    
    public long getUnreadMessageCount(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        return chatMessageRepository.countUnreadMessages(receiver.getId().toString(), sender.getId().toString());
    }
}