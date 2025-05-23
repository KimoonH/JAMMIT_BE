package com.jammit_be.user.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.Genre;
import jakarta.persistence.*;

@Entity
public class PreferredGenre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "genre_name")
    private Genre name;
    @Column(name = "genre_priority")
    private Integer priority;
}
