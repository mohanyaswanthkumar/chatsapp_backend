package com.example.chats_backend.controller;

import com.example.chats_backend.entity.User;
import com.example.chats_backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Signup (register a new user)
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED) // Return HTTP 201 (Created)
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            System.out.println("Trigger Register");
            System.out.println("User Details: " + user); // Add logging for incoming user data

            // Ensure user data is valid before registering
            if (user.getEmail() == null || user.getPassword() == null || user.getName() == null) {
                throw new IllegalArgumentException("Required fields are missing");
            }

            userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Illegal Argument Exception: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during registration.");
        }
    }


    // Login (check if user exists and validate credentials)
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {
        User loggedInUser = userService.loginUser(email, password);
        if (loggedInUser != null) {
            session.setAttribute("user", loggedInUser);
            // Store user in session
            User ut=(User)session.getAttribute("user");
            System.out.println(ut.toString()+" org"+loggedInUser.toString());
            return ResponseEntity.ok(loggedInUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        System.out.println("Profile");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }

    @GetMapping("/isLoggedIn")
    public ResponseEntity<Map<String, Boolean>> isLoggedIn(HttpSession session) {
        Map<String, Boolean> response = new HashMap<>();

        if (session.getAttribute("user") == null) {
            response.put("isLoggedIn", false);
            System.out.println("false session");
            return ResponseEntity.ok(response);
        }

        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.put("isLoggedIn", false);
            System.out.println("false user");
            return ResponseEntity.ok(response);
        }

        System.out.println("true " + user.toString());
        response.put("isLoggedIn", true);
        return ResponseEntity.ok(response);
    }

    // Fetch the list of chat partners for the logged-in user
    @GetMapping("/chat-partners")
    public ResponseEntity<List<User>> getChatPartners(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null); // Return HTTP 401 if user is not logged in
        }
        List<User> chatPartners = userService.getChatPartners(user.getId());
        return ResponseEntity.ok(chatPartners); // Return the list of chat partners with HTTP 200 (OK)
    }




    // Fetch all users except the logged-in user (for selecting chat partners)
    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsersExceptLoggedIn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null); // Return HTTP 401 if user is not logged in
        }
        List<User> users = userService.getAllUsersExceptLoggedIn(user.getId());
        return ResponseEntity.ok(users); // Return list of all users with HTTP 200 (OK)
    }

    // Update the user's profile (name, image, etc.)
    @PutMapping("/update-profile")
    public ResponseEntity<User> updateUserProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestPart(required = false) MultipartFile image, // Optional image
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // HTTP 401
        }

        // Call the service to update the profile with new or existing data
        User updatedUser = userService.updateUserProfile(user.getId(), name, email,mobile, image);
        return ResponseEntity.ok(updatedUser); // HTTP 200 with updated user
    }


    // Logout endpoint to invalidate the session
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Return HTTP 204 (No Content) on successful logout
    public void logout(HttpSession session) {
        session.invalidate(); // Invalidate the session on logout
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated.");
        }
        try {
            userService.deleteUser(loggedInUser.getId());
            session.invalidate();
            return ResponseEntity.ok("User account deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }


}
