package com.jammit_be.bandsession.entity;

import com.jammit_be.gatheringparticipant.entity.GatheringParticipant;
import com.jammit_be.userbandsession.entity.UserBandSession;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class BandSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 예) 보컬, 드럼, 일렉기타, 베이스...

    @OneToMany(mappedBy = "bandSession")
    private List<GatheringParticipant> gatheringParticipants = new ArrayList<>();


    @OneToMany(mappedBy = "bandSession")
    private List<UserBandSession> userBandSessions = new ArrayList<>();
}
