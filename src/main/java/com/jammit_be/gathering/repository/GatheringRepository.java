package com.jammit_be.gathering.repository;

import com.jammit_be.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> , GatheringRepositoryCustom {
    @Query("select g from Gathering g join fetch g.gatheringSessions where g.id = :id")
    Optional<Gathering> findByIdWithSessions(@Param("id") Long id);
}
