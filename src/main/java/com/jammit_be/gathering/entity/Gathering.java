package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseUserEntity;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.review.entity.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "gathering")
public class Gathering extends BaseUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "gathering_name", nullable = false, length = 30)
    private String name; // 모임 이름
    @Column(name = "gathering_place", nullable = false)
    private String place; // 모임 장소
    @Column(name = "gathering_description", nullable = false, length = 1000)
    private String description; // 모임 설명
    @Column(name = "gathering_song", nullable = false, length = 10)
    private String song; // 곡
    @Column(name = "gathering_thumbnail")
    private String thumbnail;
    
    // 모임 장르들 (다중 선택 가능)
    @ElementCollection
    @Column(name = "genre_name")
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "gathering_genres", joinColumns = @JoinColumn(name = "gathering_id"))
    private Set<Genre> genres = new HashSet<>();

    // 모집 중인 밴드 세션과 각 세션별 인원 정보
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GatheringSession> gatheringSessions = new ArrayList<>();

    // 참가자들 목록
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<GatheringParticipant> participants = new ArrayList<>();

    // 리뷰들
    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();
    
    public void addGenre(Genre genre) {
        this.genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        this.genres.remove(genre);
    }

    public void addGatheringSession(GatheringSession gatheringSession) {
        gatheringSession.setGathering(this);
        this.gatheringSessions.add(gatheringSession);
    }

    public void removeGatheringSession(GatheringSession gatheringSession) {
        this.gatheringSessions.remove(gatheringSession);
        gatheringSession.setGathering(null);
    }

}
