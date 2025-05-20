package com.jammit_be.usergenre.entity;

import com.jammit_be.genre.entity.Genre;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;

@Entity
public class UserGenre {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Genre genre;
}
