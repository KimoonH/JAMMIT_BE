package com.jammit_be.user.entity;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.exception.AlertException;
import com.jammit_be.gathering.entity.GatheringParticipant;
import com.jammit_be.review.entity.Review;
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
import java.util.*;

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
    @Column(nullable = true, length = 30)
    private String nickname; // 닉네임
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private OauthPlatform oauthPlatform;
    private String orgFileName;
    private String profileImagePath;

    // 내가 참가한 모임들
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GatheringParticipant> participants = new ArrayList<>();

    // 내가 선택한 곡장르들
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final Set<PreferredGenre> preferredGenres = new HashSet<>();

    // 내가 선택한 밴드 세션들
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<PreferredBandSession> userBandSessions = new HashSet<>();

    // 내가 작성한 리뷰들
    @OneToMany(mappedBy = "reviewer")
    private List<Review> writtenReviews = new ArrayList<>();

    // 내가 받은 리뷰들
    @OneToMany(mappedBy = "reviewee")
    private List<Review> receivedReviews = new ArrayList<>();

    @Builder
    public User(String email, String password, String username, String nickname,OauthPlatform oauthPlatform) {
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.username = Objects.requireNonNull(username);
        this.nickname = Objects.requireNonNull(nickname);
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
        updatePreferredGenres(updateUserRequest.getPreferredGenres());
        updatePreferredBandSessions(updateUserRequest.getPreferredBandSessions());
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

    public void updatePreferredGenres(List<Genre> genres) {
        this.preferredGenres.clear();
        if (genres != null && !genres.isEmpty()) {
            for (int i = 0; i < genres.size(); i++) {
                this.preferredGenres.add(PreferredGenre.create(this, genres.get(i), i));
            }
        }
    }

    public void updatePreferredBandSessions(List<BandSession> bandSessions) {
        this.userBandSessions.clear();;
        if (bandSessions != null && !bandSessions.isEmpty()) {
            for (int i = 0; i < bandSessions.size(); i++) {
                this.userBandSessions.add(PreferredBandSession.create(this, bandSessions.get(i), i));
            }
        }
    }

    // 프로필 이미지 변경
    public void changeProfileImage(String orgFileName, String profileImagePath) {
        this.orgFileName = orgFileName;
        this.profileImagePath = profileImagePath;
    }
}
