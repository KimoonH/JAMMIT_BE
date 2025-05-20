package com.jammit_be.gathering.entity;

import com.jammit_be.gatheringparticipant.entity.GatheringParticipant;
import com.jammit_be.review.entity.Review;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Gathering {

    @Id @GeneratedValue
    private Long id;
    @Column(name = "gathering_name", nullable = false, length = 30)
    private String name; // 모임 이름
    @Column(name = "gathering_place", nullable = false)
    private String place; // 모임 장소
    @Column(name = "gathering_description", nullable = false, length = 1000)
    private String description; // 모임 설명
    @Column(name = "gathering_song", nullable = false, length = 10)
    private String song; // 곡
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 참가자들 목록
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<GatheringParticipant> participants = new ArrayList<>();

    // 리뷰들
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();
}
