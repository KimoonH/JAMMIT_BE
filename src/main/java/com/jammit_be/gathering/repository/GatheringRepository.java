package com.jammit_be.gathering.repository;

import com.jammit_be.gathering.entity.Gathering;
import com.jammit_be.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> , GatheringRepositoryCustom {
    @EntityGraph(value = "Gathering.withSessionsAndUsers")
    @Query("select g from Gathering g where g.id = :id")
    Optional<Gathering> findByIdWithSessions(@Param("id") Long id);
    
    /**
     * 사용자가 생성한 모임 목록 조회 (취소 포함 여부에 따라 필터링)
     * @param createdBy 모임 생성자
     * @param includeCanceled 취소된 모임 포함 여부
     * @param pageable 페이징 정보
     * @return 사용자가 생성한 모임 목록 (페이지)
     */
    @EntityGraph(value = "Gathering.withUsers")
    @Query("SELECT g FROM Gathering g WHERE g.createdBy = :createdBy " +
           "AND (:includeCanceled = true OR g.status != 'CANCELED') " +
           "ORDER BY g.createdAt DESC")
    Page<Gathering> findByCreatedBy(@Param("createdBy") User createdBy, 
                                    @Param("includeCanceled") boolean includeCanceled,
                                    Pageable pageable);
                                    
    /**
     * 사용자가 생성한 모든 모임 수 카운트
     * @param createdBy 모임 생성자
     * @return 사용자가 생성한 모든 모임 수
     */
    @Query("SELECT COUNT(g) FROM Gathering g WHERE g.createdBy = :createdBy")
    long countByCreatedBy(@Param("createdBy") User createdBy);
    
    /**
     * 사용자가 생성한 모임 중 COMPLETED 상태인 모임 수 카운트
     * @param createdBy 모임 생성자
     * @return 사용자가 생성한 모임 중 COMPLETED 상태인 모임 수
     */
    @Query("SELECT COUNT(g) FROM Gathering g WHERE g.createdBy = :createdBy AND g.status = 'COMPLETED'")
    long countByCreatedByAndStatusCompleted(@Param("createdBy") User createdBy);
                                    
    @Override
    @EntityGraph(value = "Gathering.withUsers")
    Optional<Gathering> findById(Long id);
    
    @Override
    @EntityGraph(value = "Gathering.withUsers")
    List<Gathering> findAll();
}
