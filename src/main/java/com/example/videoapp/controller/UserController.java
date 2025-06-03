package com.example.videoapp.controller;

import com.example.videoapp.dto.LoginResponseDto;
import com.example.videoapp.model.User;
import com.example.videoapp.repository.UserRepository;
import com.example.videoapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User foundUser = userRepository.findByUsername(user.getUsername());
        if (foundUser != null && passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            String token = jwtUtil.generateToken(foundUser.getUsername(), foundUser.getId());
            LoginResponseDto response = new LoginResponseDto(token, foundUser.getId(), foundUser.getUsername());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Credenziali non valide");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username già esistente");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        // Rimuovi la password dalla risposta
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }
}