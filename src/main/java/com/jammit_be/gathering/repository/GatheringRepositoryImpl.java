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
public class GatheringRepositoryImpl implements GatheringRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Gathering> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable) {

        QGathering gathering = QGathering.gathering;
        QGatheringSession session = QGatheringSession.gatheringSession;

        BooleanBuilder whereConditions = buildWhereConditions(genres, sessions, gathering, session);

        List<Long> gatheringIds = getGatheringIds(gathering, session, whereConditions, pageable);

        if (gatheringIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Gathering> content = getGatheringsByIds(gathering, gatheringIds, pageable);

        long total = getTotalCount(gathering, session,  whereConditions);

        return new PageImpl<>(content, pageable, total);
    }


    /**
     * 조건에 맞는 ID만 조회 (DB 레벨 페이징)
     */
    private List<Long> getGatheringIds(QGathering gathering, QGatheringSession session,
                                       BooleanBuilder whereConditions, Pageable pageable) {

        JPQLQuery<Long> query = queryFactory
                .select(gathering.id)
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .where(whereConditions);

        // 정렬 적용
        applyOrderByForIds(query, gathering, pageable);

        return query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * ID 기반으로 FetchJoin 실행
     */
    private List<Gathering> getGatheringsByIds(QGathering gathering, List<Long> gatheringIds, Pageable pageable) {

        JPQLQuery<Gathering> query = queryFactory
                .selectFrom(gathering)
                .leftJoin(gathering.gatheringSessions).fetchJoin()
                .leftJoin(gathering.genres).fetchJoin()
                .where(gathering.id.in(gatheringIds));

        // 정렬 적용 (결과 순서 보장)
        applyOrderBy(query, gathering, pageable);

        return query.fetch();
    }

    /**
     * ID 조회용 정렬 적용
     */
    private void applyOrderByForIds(JPQLQuery<Long> query, QGathering gathering, Pageable pageable) {
        boolean orderApplied = false;

        for (Sort.Order order : pageable.getSort()) {
            switch (order.getProperty()) {
                case "viewCount":
                    query.orderBy(order.isAscending() ?
                            gathering.viewCount.asc() : gathering.viewCount.desc());
                    orderApplied = true;
                    break;
                case "recruitDeadline":
                    query.orderBy(order.isAscending() ?
                            gathering.recruitDeadline.asc() : gathering.recruitDeadline.desc());
                    orderApplied = true;
                    break;
            }
        }

        if (!orderApplied) {
            query.orderBy(gathering.recruitDeadline.asc());
        }
    }


    /**
     * 전체 개수 조회
     */
    private long getTotalCount(QGathering gathering, QGatheringSession session, BooleanBuilder whereConditions) {
        Long total = queryFactory
                .select(gathering.countDistinct())
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .where(whereConditions)
                .fetchOne();

        return total != null ? total : 0L;
    }


    /**
     * FetchJoin 쿼리용 정렬 적용
     */
    private void applyOrderBy(JPQLQuery<Gathering> query, QGathering gathering, Pageable pageable) {
        boolean orderApplied = false;

        for (Sort.Order order : pageable.getSort()) {
            switch (order.getProperty()) {
                case "viewCount":
                    query.orderBy(order.isAscending() ?
                            gathering.viewCount.asc() : gathering.viewCount.desc());
                    orderApplied = true;
                    break;
                case "recruitDeadline":
                    query.orderBy(order.isAscending() ?
                            gathering.recruitDeadline.asc() : gathering.recruitDeadline.desc());
                    orderApplied = true;
                    break;
            }
        }

        if (!orderApplied) {
            query.orderBy(gathering.recruitDeadline.asc());
        }
    }


    /**
     * WHERE 조건 빌딩
     */
    private BooleanBuilder buildWhereConditions(List<Genre> genres, List<BandSession> sessions,
                                                QGathering gathering, QGatheringSession session) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(gathering.status.eq(GatheringStatus.RECRUITING));

        if (isNotEmpty(genres)) {
            builder.and(gathering.genres.any().in(genres));
        }
        if (isNotEmpty(sessions)) {
            builder.and(session.name.in(sessions));
        }

        return builder;
    }

    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
