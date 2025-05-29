package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gathering_session")
public class GatheringSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @Enumerated(EnumType.STRING)
    @Column(name = "band_session_name", nullable = false)
    private BandSession name;

    @Column(nullable = false)
    private int recruitCount; // 해당 세션 모집 인원 수
    
    @Column(nullable = false)
    private int currentCount;// 현재 모집된 인원 수

    public static GatheringSession create(BandSession bandSession, int recruitCount) {

        GatheringSession session = new GatheringSession();
        session.name = bandSession;
        session.recruitCount = recruitCount;
        session.currentCount = 0; // 생성 시점은 항상 0
        return session;
    }

    public void incrementCurrentCount() {
        this.currentCount += 1;
    }

    public void decrementCurrentCount() {
        if(this.currentCount > 0) {
            this.currentCount -= 1;
        }
    }
}