package com.jammit_be.gathering.repository;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.entity.Gathering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GatheringRepositoryCustom {
    Page<Gathering> findGatherings(List<Genre> genres, List<BandSession> sessions, Pageable pageable);
}
