package com.jammit_be.review.entity;

import com.jammit_be.common.entity.BaseEntity;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "review")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Review.withUsers",
        attributeNodes = {
            @NamedAttributeNode("reviewer"),
            @NamedAttributeNode("reviewee")
        }
    ),
    @NamedEntityGraph(
        name = "Review.withUsersAndGathering",
        attributeNodes = {
            @NamedAttributeNode("reviewer"),
            @NamedAttributeNode("reviewee"),
            @NamedAttributeNode("gathering")
        }
    )
})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = true)
    private String content; // 리뷰 내용 (선택 입력)

    // 체크박스 리뷰 옵션
    @Column
    private boolean isPracticeHelped; // 연주 실력이 좋아요
    @Column
    private boolean isGoodWithMusic; // 곡 준비를 잘 해왔어요
    @Column
    private boolean isGoodWithOthers; // 다른 파트와의 호흡이 잘 맞아요
    @Column
    private boolean isSharesPracticeResources; // 악보나 연습 자료를 잘 공유해줬어요
    @Column
    private boolean isManagingWell; // 분위기를 잘 이끌어요
    @Column
    private boolean isHelpful; // 팀워크가 좋고 함께 연주하기 편했어요
    @Column
    private boolean isGoodLearner; // 빨리 배워서 잘 따라해줘요
    @Column
    private boolean isKeepingPromises; // 합주 시간 약속을 잘 지켜요

    // 평가한 사람 (리뷰 작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // 평가 대상 (리뷰를 받는 사람)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;
}
