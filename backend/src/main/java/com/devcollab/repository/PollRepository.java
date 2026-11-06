package com.devcollab.repository;

import com.devcollab.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PollRepository extends JpaRepository<Poll, UUID> {
    List<Poll> findByChannelIdOrderByCreatedAtDesc(UUID channelId);
    Optional<Poll> findByIdAndChannelId(UUID id, UUID channelId);
}
