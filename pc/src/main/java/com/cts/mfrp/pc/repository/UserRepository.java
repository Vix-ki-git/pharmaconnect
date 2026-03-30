package com.cts.mfrp.pc.repository;

import com.cts.mfrp.pc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Optional<User> findByEmail(String email);
}