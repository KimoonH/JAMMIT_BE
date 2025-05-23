package com.jammit_be.user.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import jakarta.persistence.*;

@Entity
public class PreferredBandSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "band_session_name")
    private BandSession name;
    @Column(name = "band_session_priority")
    private Integer priority;
}
