package com.jammit_be.user.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    public static PreferredBandSession create(User user, BandSession bandSession, Integer priority) {
        return PreferredBandSession.builder()
                .user(user)
                .name(bandSession)
                .priority(priority)
                .build();
    }
    
    public void updatePriority(Integer priority) {
        this.priority = priority;
    }
}
