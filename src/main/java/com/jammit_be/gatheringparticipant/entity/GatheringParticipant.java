package com.jammit_be.gatheringparticipant.entity;

import com.jammit_be.bandsession.entity.BandSession;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;

@Entity
public class GatheringParticipant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "band_session_id")
    private BandSession bandSession;

}
