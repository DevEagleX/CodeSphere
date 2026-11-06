package com.devcollab.repository;

import com.devcollab.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, UUID> {
    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);
    Optional<ChannelMember> findByChannelIdAndUserId(UUID channelId, UUID userId);
    List<ChannelMember> findByUserId(UUID userId);
    long countByChannelId(UUID channelId);
}
