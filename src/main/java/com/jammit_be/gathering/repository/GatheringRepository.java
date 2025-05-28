package com.jammit_be.gathering.repository;

import com.jammit_be.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> , GatheringRepositoryCustom {

}
