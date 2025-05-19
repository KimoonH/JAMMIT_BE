package com.jammit_be.user.entity;

import com.jammit_be.common.exception.AlertException;
import com.jammit_be.user.dto.request.UpdateImageRequest;
import com.jammit_be.user.dto.request.UpdateUserRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30, unique = true)
    private String email;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 30)
    private String username;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private OauthPlatform oauthPlatform;

    private String orgFileName;
    private String profileImagePath;

    @Builder
    public User(String email, String password, String username, OauthPlatform oauthPlatform) {
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.username = Objects.requireNonNull(username);
        this.oauthPlatform = Objects.requireNonNullElse(oauthPlatform, OauthPlatform.NONE);
    }

    public void modify(UpdateUserRequest updateUserRequest, PasswordEncoder passwordEncoder) {
        if (updateUserRequest.getEmail() != null) {
            this.email = updateUserRequest.getEmail();
        }
        if (updateUserRequest.getUsername() != null) {
            this.username = updateUserRequest.getUsername();
        }
        if (updateUserRequest.getPassword() != null) {
            this.password = passwordEncoder.encode(updateUserRequest.getPassword());
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfileImage(UpdateImageRequest updateImageRequest) {
        var orgFileName = updateImageRequest.getOrgFileName();
        var profileImagePath = updateImageRequest.getProfileImagePath();
        if (profileImagePath != null) {
            if (StringUtils.isEmpty(orgFileName)) throw new AlertException("프로필 이미지 수정시 원본 파일 이름은 필수입니다.");
            this.orgFileName = orgFileName;
            this.profileImagePath = profileImagePath;
        } else {
            this.orgFileName = null;
            this.profileImagePath = null;
        }
    }

}
