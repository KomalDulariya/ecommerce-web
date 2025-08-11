package com.example.auth.controller;

import com.example.auth.dto.JwtResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.model.User;
import com.example.auth.repo.UserRepository;
import com.example.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username taken"));
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole("USER");
        userRepository.save(u);

        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var opt = userRepository.findByUsername(req.getUsername());
        if (opt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));

        var user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        var opt = userRepository.findByUsername(principal.getName());
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "not found"));
        var user = opt.get();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt()
        ));
    }
}
