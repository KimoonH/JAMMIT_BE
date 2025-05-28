package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseUserEntity;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.review.entity.Review;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private String thumbnail; // 이미지
    @Column(name = "gathering_view_count", nullable = false)
    private int viewCount = 0; // 조회수
    @Column(name = "gathering_datetime", nullable = false)
    private LocalDateTime gatheringDateTime; // 모집일

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDateTime recruitDeadline; // 모집 마감일
    
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

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePlace(String place) {
        this.place = place;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeSong(String song) {
        this.song = song;
    }

    public void changeThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void changeGatheringDateTime(LocalDateTime gatheringDateTime) {
        this.gatheringDateTime = gatheringDateTime;
    }

    public void changeRecruitDeadline(LocalDateTime recruitDeadline) {
        this.recruitDeadline = recruitDeadline;
    }


    public void changeGenres(Set<Genre> genres) {
        this.genres = new HashSet<>(genres);
    }

    public void updateGatheringSessions(List<GatheringSession> sessions) {
        this.gatheringSessions.clear();
        for (GatheringSession s : sessions) {
            s.setGathering(this); // 연관관계 주인 설정
            this.gatheringSessions.add(s);
        }
    }

    public static Gathering create(String name
                                , String thumbnail
                                , String place
                                , String description
                                , String song
                                , LocalDateTime gatheringDateTime
                                , LocalDateTime recruitDeadline
                                , Set<Genre> genres
                                , List<GatheringSession> sessions
                                , User user
                                   )

    {
        Gathering gathering = new Gathering();
        gathering.name = name;
        gathering.thumbnail = thumbnail;
        gathering.place = place;
        gathering.description = description;
        gathering.song = song;
        gathering.gatheringDateTime = gatheringDateTime;
        gathering.recruitDeadline = recruitDeadline;
        gathering.genres.addAll(genres);
        gathering.createdBy = user;

        for(GatheringSession session : sessions) {
            session.setGathering(gathering);
            gathering.gatheringSessions.add(session);
        }

        return gathering;
    }
}
