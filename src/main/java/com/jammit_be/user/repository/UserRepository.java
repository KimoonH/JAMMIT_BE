package com.jammit_be.user.repository;

import com.jammit_be.user.entity.OauthPlatform;
import com.jammit_be.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"preferredGenres", "userBandSessions"})
    Optional<User> findUserByEmail(String email);

    @EntityGraph(attributePaths = {"preferredGenres", "userBandSessions"})
    Optional<User> findUserByEmailAndOauthPlatform(String email, OauthPlatform oAuthPlatform);

    @EntityGraph(attributePaths = {"preferredGenres", "userBandSessions"})
    Optional<User> findUserById(Long userId);

    boolean existsUserByEmail(String email);

    boolean existsUserByUsername(String username);
    
    @Modifying
    @Query("DELETE FROM PreferredGenre pg WHERE pg.user.id = :userId")
    void deleteAllPreferredGenresByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM PreferredBandSession pbs WHERE pbs.user.id = :userId")
    void deleteAllPreferredBandSessionsByUserId(@Param("userId") Long userId);
}
