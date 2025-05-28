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

    private JPAQueryFactory queryFactory;

    @Override
    public Page<Gathering> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable) {
        QGathering gathering = QGathering.gathering;
        QGatheringSession session = QGatheringSession.gatheringSession;

        JPQLQuery<Gathering> query = queryFactory
                .selectDistinct(gathering)
                .from(gathering)
                .join(gathering.gatheringSessions, session).fetchJoin();

        BooleanBuilder builder = new BooleanBuilder();

        if (genres != null && !genres.isEmpty()) {
            builder.and(gathering.genres.any().in(genres));
        }
        if (sessions != null && !sessions.isEmpty()) {
            builder.and(session.name.in(sessions));
        }

        // 3. 정렬
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

        List<Gathering> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();



        long total = query
                .select(gathering.countDistinct())
                .from(gathering)
                .join(gathering.gatheringSessions, session)
                .where(builder)
                .fetchOne();


        return new PageImpl<>(content, pageable, total);
    }


}
