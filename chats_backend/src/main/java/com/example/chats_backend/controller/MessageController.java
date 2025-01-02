package com.example.chats_backend.controller;

import com.example.chats_backend.entity.Message;
import com.example.chats_backend.entity.User;
import com.example.chats_backend.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Get chat history between two users
    @GetMapping("/{friendUserId}")
    public ResponseEntity<List<Message>> getChatHistory(HttpSession session, @PathVariable Long friendUserId) {
        try {
            User user = (User) session.getAttribute("user");

            if (user == null) {
                System.out.println("session fail");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Return HTTP 401 if user is not logged in
            }
            List<Message> chatHistory = messageService.getChatHistory(user.getId(), friendUserId);
            System.out.println("individ-chat ok"+chatHistory+" "+friendUserId);
            return ResponseEntity.ok(chatHistory); // Return the chat history with HTTP 200 (OK)
        } catch (Exception e) {
            System.out.println("individ-chat not ok");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return HTTP 500 on error
        }
    }

    // Get all chats (sent or received) by the logged-in user
    @GetMapping("/all-chats")
    public ResponseEntity<List<Map<String, Object>>> getAllChats(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                System.out.println("User not logged in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Return HTTP 401 if user is not logged in
            }
            List<Map<String, Object>> allChats = messageService.getAllChats(user.getId());
            System.out.println("Chats fetched successfully: " + allChats);
            return ResponseEntity.ok(allChats); // Return all chats with HTTP 200 (OK)
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return HTTP 500 on error
        }
    }

    // Send a new message from the logged-in user to another user
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Message> sendMessage(HttpSession session, @PathVariable Long receiverId, @RequestBody String content) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Return HTTP 401 if user is not logged in
            }
            Message newMessage = messageService.sendMessage(user.getId(), receiverId, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMessage); // Return the sent message with HTTP 201 (Created)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return HTTP 500 on error
        }
    }



}
