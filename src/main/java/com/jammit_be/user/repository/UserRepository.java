package com.jammit_be.user.repository;

import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByEmailAndOauthPlatform(String email, OauthPlatform oAuthPlatform);

    Optional<User> findUserById(Long userId);

    boolean existsUserByEmail(String email);

    boolean existsUserByUsername(String username);
}
