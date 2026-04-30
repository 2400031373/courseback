package com.example.backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private List<User> users = new ArrayList<>();

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setId(UUID.randomUUID().toString());
        users.add(user);
        return user;
    }

    @PostMapping("/login")
    public User login(@RequestBody User request) {
        return users.stream()
            .filter(u ->
                u.getEmail().equals(request.getEmail()) &&
                u.getPassword().equals(request.getPassword()) &&
                u.getRole().equals(request.getRole()))
            .findFirst()
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Login"));
    }
}