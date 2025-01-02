package com.example.chats_backend.repository;

import com.example.chats_backend.entity.Message;
import com.example.chats_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiver(User sender, User receiver);
    List<Message> findBySenderOrReceiver(User sender, User receiver);  // New query to get all chats of a user
}

