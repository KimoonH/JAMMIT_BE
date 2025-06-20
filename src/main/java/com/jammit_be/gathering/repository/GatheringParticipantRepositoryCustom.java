package com.jammit_be.gathering.repository;

import com.jammit_be.gathering.dto.response.CompletedGatheringResponse;
import com.jammit_be.user.entity.User;

import java.util.List;

public interface GatheringParticipantRepositoryCustom {

    List<CompletedGatheringResponse> findCompletedGatheringsByUser(User  user);
}
