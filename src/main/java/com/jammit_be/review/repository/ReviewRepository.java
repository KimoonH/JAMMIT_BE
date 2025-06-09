package com.jammit_be.review.repository;

import com.jammit_be.review.entity.Review;
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
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(value = "Review.withUsers")
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :userId")
    List<Review> findAllByReviewerId(@Param("userId") Long userId);

    @EntityGraph(value = "Review.withUsers")
    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :userId")
    List<Review> findAllByRevieweeId(@Param("userId") Long userId);

    @EntityGraph(value = "Review.withUsersAndGathering")
    @Query("SELECT r FROM Review r WHERE r.reviewee.id = :userId")
    Page<Review> findAllByRevieweeId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(value = "Review.withUsersAndGathering")
    @Query("SELECT r FROM Review r WHERE r.gathering.id = :gatheringId")
    List<Review> findAllByGatheringId(@Param("gatheringId") Long gatheringId);

    @EntityGraph(value = "Review.withUsersAndGathering")
    @Query("SELECT r FROM Review r WHERE r.reviewer.id = :reviewerId AND r.reviewee.id = :revieweeId AND r.gathering.id = :gatheringId")
    Optional<Review> findByReviewerIdAndRevieweeIdAndGatheringId(
            @Param("reviewerId") Long reviewerId,
            @Param("revieweeId") Long revieweeId,
            @Param("gatheringId") Long gatheringId);

    @Override
    @EntityGraph(value = "Review.withUsers")
    Optional<Review> findById(Long id);

    @Override
    @EntityGraph(value = "Review.withUsers")
    List<Review> findAll();
} 