package com.devcollab.mapper;

import com.devcollab.dto.ChannelMemberResponse;
import com.devcollab.dto.ChannelResponse;
import com.devcollab.entity.Channel;
import com.devcollab.entity.ChannelMember;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

    public ChannelResponse toChannelResponse(Channel channel, long memberCount) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .workspaceId(channel.getWorkspace().getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .visibility(channel.getVisibility().name())
                .createdBy(channel.getCreatedBy().getId())
                .createdByUsername(channel.getCreatedBy().getUsername())
                .memberCount(memberCount)
                .createdAt(channel.getCreatedAt())
                .build();
    }

    public ChannelMemberResponse toMemberResponse(ChannelMember member) {
        return ChannelMemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
