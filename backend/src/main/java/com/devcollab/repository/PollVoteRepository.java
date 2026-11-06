package com.devcollab.repository;

import com.devcollab.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollVoteRepository extends JpaRepository<PollVote, UUID> {
    Optional<PollVote> findByPollIdAndUserId(UUID pollId, UUID userId);
    List<PollVote> findByPollId(UUID pollId);
    long countByPollOptionId(UUID pollOptionId);
}
