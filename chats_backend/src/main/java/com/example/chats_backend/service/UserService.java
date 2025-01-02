package com.example.chats_backend.service;

import com.example.chats_backend.entity.Message;
import com.example.chats_backend.entity.User;
import com.example.chats_backend.repository.UserRepository;
import com.example.chats_backend.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Register user (Signup)
    public User registerUser(User user) {
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);  // Return type is Optional<User>

        if (userOptional.isPresent()) {
            User user = userOptional.get();  // Get the actual User object from Optional
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;  // Successfully logged in
            }
        }
        return null;  // Invalid credentials or user not found
    }

    public List<User> getChatPartners(Long loggedInUserId) {
        User loggedInUser = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Message> messages = messageRepository.findBySenderOrReceiver(loggedInUser, loggedInUser);

        return messages.stream()
                .map(message -> {
                    return message.getSender().equals(loggedInUser) ? message.getReceiver() : message.getSender();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    // Fetch user by ID (for profile information)
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User updateUserProfile(Long userId, String newName, String newEmail, String mobile, MultipartFile newImage) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (newName != null && !newName.isEmpty()) user.setName(newName);
        if (newEmail != null && !newEmail.isEmpty()) user.setEmail(newEmail);
        if (mobile != null && !mobile.isEmpty()) user.setMobile(mobile);

        if (newImage != null && !newImage.isEmpty()) {
            try {
                user.setImage(newImage.getBytes()); // Set image as byte array
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert image to byte array", e);
            }
        }
        return userRepository.save(user);
    }

    // Fetch all users except the logged-in user (for selecting chat partners)
    public List<User> getAllUsersExceptLoggedIn(Long loggedInUserId) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(user -> !user.getId().equals(loggedInUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
