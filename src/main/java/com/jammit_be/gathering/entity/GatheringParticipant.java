package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private boolean canceled = false;
    @Column(nullable = false)
    private boolean approved = false;
    @Column(nullable = false)
    private boolean rejected = false;

    private GatheringParticipant(User user, Gathering gathering, BandSession name, boolean approved, boolean canceled, boolean rejected) {
        this.user = user;
        this.gathering = gathering;
        this.name = name;
        this.approved = approved;
        this.canceled = canceled;
        this.rejected = rejected;
    }

    public static GatheringParticipant pending(User user, Gathering gathering, BandSession name) {
        return new GatheringParticipant(user, gathering, name, false, false, false);
    }

    public void approve() {
        this.approved = true;
        this.canceled = false;
        this.rejected = false;
    }

    public void cancel() {
        this.canceled = true;
        this.approved = false;
        this.rejected = false;
    }

    public void reject() {
        this.rejected = true;
        this.approved = false;
        this.canceled = false;
    }
}
