package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gathering_participant")
public class GatheringParticipant extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;
    @Enumerated(EnumType.STRING)
    @Column(name = "band_session_name", nullable = false)
    private BandSession name;
    @Column(nullable = false)
    private boolean isApproved;
}
