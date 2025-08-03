package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseUserEntity;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.review.entity.Review;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "gathering")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Gathering.withUsers",
        attributeNodes = {
            @NamedAttributeNode("createdBy"),
            @NamedAttributeNode("updatedBy")
        }
    ),
    @NamedEntityGraph(
        name = "Gathering.withSessionsAndUsers",
        attributeNodes = {
            @NamedAttributeNode("gatheringSessions"),
            @NamedAttributeNode("createdBy"),
            @NamedAttributeNode("updatedBy")
        }
    )
})
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
    @Column(name = "gathering_thumbnail")
    private String thumbnail; // 이미지
    @Column(name = "gathering_view_count", nullable = false)
    private int viewCount = 0; // 조회수
    @Column(name = "gathering_datetime", nullable = false)
    private LocalDateTime gatheringDateTime; // 모집일

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDateTime recruitDeadline; // 모집 마감일
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GatheringStatus status = GatheringStatus.RECRUITING; // 모임 상태 (기본값: 멤버 모집 중)
    
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
    
    /**
     * 모임을 취소 상태로 변경합니다.
     */
    public void cancel() {
        this.status = GatheringStatus.CANCELED;
    }
    
    /**
     * 모임을 모집 완료 상태로 변경합니다.
     */
    public void confirm() {
        if (this.status == GatheringStatus.RECRUITING) {
            this.status = GatheringStatus.CONFIRMED;
        }
    }
    
    /**
     * 모임을 완료 상태로 변경합니다.
     * 실제 합주가 완료되었고, 참가자들이 서로를 리뷰할 수 있는 상태로 변경합니다.
     */
    public void complete() {
        if (this.status == GatheringStatus.CONFIRMED) {
            this.status = GatheringStatus.COMPLETED;
            
            // 모든 승인된 참가자를 참여 완료 상태로 변경
            for (GatheringParticipant participant : this.participants) {
                if (participant.isApproved()) {
                    participant.complete();
                }
            }
        }
    }

    /**
     * 모임에 참가 신청이 가능한 상태인지 확인합니다.
     * @return 참가 신청 가능 상태이면 true
     */
    public boolean isJoinable() {
        return this.status.isJoinable();
    }

    /**
     * 모임 생성
     */
    public static Gathering create(String name
                                , String thumbnail
                                , String place
                                , String description
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
        gathering.gatheringDateTime = gatheringDateTime;
        gathering.recruitDeadline = recruitDeadline;
        gathering.genres.addAll(genres);
        gathering.createdBy = user;
        gathering.status = GatheringStatus.RECRUITING;

        for(GatheringSession session : sessions) {
            session.setGathering(gathering);
            gathering.gatheringSessions.add(session);
        }

        return gathering;
    }
}
