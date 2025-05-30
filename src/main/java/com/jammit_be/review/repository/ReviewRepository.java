package com.jammit_be.review.repository;

import com.jammit_be.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :userId")
    List<Review> findAllByReviewerId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :userId")
    List<Review> findAllByRevieweeId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r WHERE r.gathering.id = :gatheringId")
    List<Review> findAllByGatheringId(@Param("gatheringId") Long gatheringId);

    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId AND r.reviewee.id = :revieweeId AND r.gathering.id = :gatheringId")
    Optional<Review> findByReviewerIdAndRevieweeIdAndGatheringId(
            @Param("reviewerId") Long reviewerId,
            @Param("revieweeId") Long revieweeId,
            @Param("gatheringId") Long gatheringId);
} 