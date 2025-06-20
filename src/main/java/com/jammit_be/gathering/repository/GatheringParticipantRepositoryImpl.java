package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.GatheringStatus;
import com.jammit_be.common.enums.ParticipantStatus;
import com.jammit_be.gathering.dto.response.CompletedGatheringResponse;
import com.jammit_be.gathering.entity.QGathering;
import com.jammit_be.gathering.entity.QGatheringParticipant;
import com.jammit_be.gathering.entity.QGatheringSession;
import com.jammit_be.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class GatheringParticipantRepositoryImpl implements GatheringParticipantRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CompletedGatheringResponse> findCompletedGatheringsByUser(User user) {
        if (user == null) {
            // user가 null이면 예외 대신 빈 리스트 반환
            return List.of(); // 또는 Collections.emptyList();
        }

        QGathering gathering = QGathering.gathering;
        QGatheringParticipant gatheringParticipant = QGatheringParticipant.gatheringParticipant;
        QGatheringSession gatheringSession = QGatheringSession.gatheringSession;

        BooleanBuilder builder = new BooleanBuilder();

        // 로그인한 사용자가 null이 아닌 경우에만 조건 추가
        if (user != null) {
            builder.and(gatheringParticipant.user.eq(user));
        }
        // 참가자의 상태가 COMPLETED인 경우만 조회
        builder.and(gatheringParticipant.status.eq(ParticipantStatus.COMPLETED));
        // 모임 자체가 COMPLETED 상태인 경우만 조회
        builder.and(gathering.status.eq(GatheringStatus.COMPLETED));

        // 모임 목록을 조회합니다.
        return queryFactory
                .select(Projections.constructor(CompletedGatheringResponse.class,
                        gathering.id,
                        gathering.name,
                        gathering.thumbnail,
                        gathering.gatheringDateTime,
                        gathering.place,

                        // totalRecruit: 해당 모임의 모든 세션 모집 정원의 합
                        JPAExpressions.select(gatheringSession.recruitCount.sum())
                                .from(gatheringSession)
                                .where(gatheringSession.gathering.eq(gathering)),

                        // totalCurrent: 해당 모임의 모든 세션 현재 인원의 합
                        JPAExpressions.select(gatheringSession.currentCount.sum())
                                .from(gatheringSession)
                                .where(gatheringSession.gathering.eq(gathering)),

                        gathering.status.stringValue()
                ))
                .from(gatheringParticipant) // 참가자를 기준으로 시작
                .join(gatheringParticipant.gathering, gathering)
                .where(builder)
                .distinct()
                .fetch();
    }
}
