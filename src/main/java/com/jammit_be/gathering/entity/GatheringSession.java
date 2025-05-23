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
    private int currentCount; // 현재 모집된 인원 수
    
}