package com.alxy.authservice.Repository;

import com.alxy.authservice.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findUserByUsername(String username);

    User findUserByUserId(String userId);

    User getUserByEmail(String email);
}