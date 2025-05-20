package com.jammit_be.userbandsession.entity;

import com.jammit_be.bandsession.entity.BandSession;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;

@Entity
public class UserBandSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private BandSession bandSession;
}
