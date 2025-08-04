package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.common.enums.GatheringStatus;
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

import java.util.Collections;
import java.util.List;

import static com.jammit_be.gathering.entity.QGathering.gathering;

@RequiredArgsConstructor
public class GatheringRepositoryImpl implements GatheringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Gathering> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable) {

        QGathering gathering = QGathering.gathering;
        QGatheringSession session = QGatheringSession.gatheringSession;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(gathering.status.eq(GatheringStatus.RECRUITING));

        if (genres != null && !genres.isEmpty()) {
            builder.and(gathering.genres.any().in(genres));
        }
        if (sessions != null && !sessions.isEmpty()) {
            builder.and(session.name.in(sessions));
        }

        // 🚀 FetchJoin 적용 - 핵심 부분!
        JPQLQuery<Gathering> query = queryFactory
                .selectDistinct(gathering)
                .from(gathering)
                .leftJoin(gathering.gatheringSessions, session).fetchJoin()
                .leftJoin(gathering.genres).fetchJoin()  // 🔥 N+1 해결 핵심!
                .where(builder);

        // 정렬 적용
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
            query.orderBy(gathering.recruitDeadline.asc());
        }

        // 페이징 적용
        List<Gathering> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (별도 실행)
        Long total = queryFactory
                .select(gathering.countDistinct())
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
