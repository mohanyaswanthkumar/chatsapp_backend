package com.example.chats_backend.service;

import com.example.chats_backend.entity.Message;
import com.example.chats_backend.entity.User;
import com.example.chats_backend.repository.MessageRepository;
import com.example.chats_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Get chat history between two users
    public List<Message> getChatHistory(Long userId, Long friendUserId) {
        // Create user objects for sender and receiver
        User user = new User();
        user.setId(userId);
        User friend = new User();
        friend.setId(friendUserId);

        // Fetch chat history between the two users
        List<Message> messagesFromUserToFriend = messageRepository.findBySenderAndReceiver(user, friend);
        List<Message> messagesFromFriendToUser = messageRepository.findBySenderAndReceiver(friend, user);

        messagesFromFriendToUser.stream()
                .filter(message -> !message.isIs_seen())
                .forEach(message -> message.setIs_seen(true));
        messageRepository.saveAll(messagesFromFriendToUser);

        // Combine both lists and sort by timestamp
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(messagesFromUserToFriend);
        allMessages.addAll(messagesFromFriendToUser);

        // Sort messages by timestamp (assuming Message has a timestamp field)
        allMessages.sort(Comparator.comparing(Message::getTimestamp));

        return allMessages;
    }


    // Get all messages for a specific user (sent or received)
    public List<Map<String, Object>> getAllChats(Long userId) {
        User loggedInUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Message> allMessages = messageRepository.findBySenderOrReceiver(loggedInUser, loggedInUser);

        Map<Long, Message> latestMessages = new HashMap<>();
        for (Message message : allMessages) {
            User chatPartner = message.getSender().getId().equals(userId)
                    ? message.getReceiver()
                    : message.getSender();

            if (!latestMessages.containsKey(chatPartner.getId()) ||
                    message.getTimestamp().isAfter(latestMessages.get(chatPartner.getId()).getTimestamp())) {
                latestMessages.put(chatPartner.getId(), message);
            }
        }

        return latestMessages.values().stream()
                .filter(message -> !message.getSender().getId().equals(userId)
                        || !message.getReceiver().getId().equals(userId))
                .map(message -> {
                    User chatPartner = message.getSender().getId().equals(userId)
                            ? message.getReceiver()
                            : message.getSender();

                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("friendId", chatPartner.getId());
                    chatData.put("friendName", chatPartner.getName());
                    chatData.put("friendImage", chatPartner.getImage());
                    chatData.put("lastMessage", message.getContent());
                    chatData.put("timestamp", message.getTimestamp());
                    chatData.put("currentUserId", userId);
                    chatData.put("is_seen", message.isIs_seen());
                    chatData.put("backgroundColor",
                            !message.getSender().getId().equals(userId) && !message.isIs_seen()
                                    ? "grey"
                                    : "white");
                    return chatData;
                })
                .collect(Collectors.toList());
    }


    // Send a new message
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = new User();
        sender.setId(senderId);
        User receiver = new User();
        receiver.setId(receiverId);

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setIs_seen(false);
        // Save the message to the database
        return messageRepository.save(message);
    }


}

