// INIZIO UserRepository.java
package com.example.videoapp.repository;

import com.example.videoapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
// FINE UserRepository.java