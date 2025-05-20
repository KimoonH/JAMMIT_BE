package com.jammit_be.genre.entity;

import com.jammit_be.usergenre.entity.UserGenre;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Genre {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 장르명

    @OneToMany(mappedBy = "genre")
    private List<UserGenre> userGenres = new ArrayList<>();
}
