package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.gathering.entity.QGathering;
import com.jammit_be.gathering.entity.QGatheringSession;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.jammit_be.gathering.entity.QGathering.gathering;

@RequiredArgsConstructor
public class GatheringRepositoryImpl implements GatheringRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Gathering> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable) {
        // 1. Q타입 생성
        QGathering gathering = QGathering.gathering;
        QGatheringSession session = QGatheringSession.gatheringSession;

        // 2. BooleanBuilder로 동적 where 조건 구성 (장르/세션)
        BooleanBuilder builder = new BooleanBuilder();

        if (genres != null && !genres.isEmpty()) {
            builder.and(gathering.genres.any().in(genres));
        }
        if (sessions != null && !sessions.isEmpty()) {
            builder.and(session.name.in(sessions));
        }

        // 3. 실제 데이터 쿼리 생성
        JPQLQuery<Gathering> query = queryFactory
                .selectDistinct(gathering)
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .fetchJoin()
                .where(builder);


        // 4. 정렬 조건 처리
        boolean orderApplied = false;
        for(Sort.Order order : pageable.getSort()) {
            switch (order.getProperty()) {
                case "viewCount":
                    query.orderBy(order.isAscending() ? gathering.viewCount.asc() : gathering.viewCount.desc());
                    orderApplied = true;
                    break;
                case "recruitDeadline":
                    query.orderBy(order.isAscending() ? gathering.recruitDeadline.asc() : gathering.recruitDeadline.desc());
                    orderApplied = true;
                    break;
            }
        }

        if (!orderApplied) {
            // 기본 정렬: 마감일 오름차순
            query.orderBy(gathering.recruitDeadline.asc());
        }

        // 5. 페이징(페이지 번호, 페이지 크기) 및 결과 데이터 조회
        List<Gathering> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        // 6. 전체 개수 조회 (total count)
        // → 페이징/정렬 옵션은 필요 없으므로 새로 쿼리!
        Long total = queryFactory
                .select(gathering.countDistinct())
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .where(builder)
                .fetchOne();

        //NullPointerException 방지
        long safeTotal = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, safeTotal);
    }
}
