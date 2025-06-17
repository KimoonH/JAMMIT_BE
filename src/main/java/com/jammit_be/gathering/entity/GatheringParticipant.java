package com.jammit_be.gathering.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "gathering_participant")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "GatheringParticipant.withUser",
        attributeNodes = {
            @NamedAttributeNode("user")
        }
    ),
    @NamedEntityGraph(
        name = "GatheringParticipant.withUserAndGathering",
        attributeNodes = {
            @NamedAttributeNode("user"),
            @NamedAttributeNode("gathering")
        }
    )
})
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
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.PENDING;
    
    @Column(length = 500)
    private String introduction;

    private GatheringParticipant(User user, Gathering gathering, BandSession name, ParticipantStatus status, String introduction) {
        this.user = user;
        this.gathering = gathering;
        this.name = name;
        this.status = status;
        this.introduction = introduction;
    }

    public static GatheringParticipant pending(User user, Gathering gathering, BandSession name, String introduction) {
        return new GatheringParticipant(user, gathering, name, ParticipantStatus.PENDING, introduction);
    }

    public void approve() {
        this.status = ParticipantStatus.APPROVED;
    }

    public void cancel() {
        this.status = ParticipantStatus.CANCELED;
    }

    public void reject() {
        this.status = ParticipantStatus.REJECTED;
    }
    
    /**
     * 참가자의 상태를 참여 완료 상태로 변경합니다.
     * 실제 합주에 참여하여 완료한 상태로, 리뷰 작성이 가능합니다.
     */
    public void complete() {
        this.status = ParticipantStatus.COMPLETED;
    }
    
    public boolean isApproved() {
        return this.status.isApproved();
    }
    
    public boolean isCanceled() {
        return this.status == ParticipantStatus.CANCELED;
    }
    
    public boolean isRejected() {
        return this.status == ParticipantStatus.REJECTED;
    }

    public boolean isCompleted() {
        return this.status == ParticipantStatus.COMPLETED;
    }

    // 주최자(Host) 생성만 허용하는 팩토리 메서드
    public static GatheringParticipant createHostParticipant(User user, Gathering gathering) {
        GatheringParticipant gp = new GatheringParticipant();
        gp.user = user;
        gp.gathering = gathering;
        gp.status = ParticipantStatus.COMPLETED; // 주최자는 바로 완료로 처리
        return gp;
    }
}
