package com.devcollab.mapper;

import com.devcollab.dto.DiscussionResponse;
import com.devcollab.entity.Discussion;
import org.springframework.stereotype.Component;

@Component
public class DiscussionMapper {

    public DiscussionResponse toResponse(Discussion discussion) {
        return DiscussionResponse.builder()
                .id(discussion.getId())
                .channelId(discussion.getChannel().getId())
                .authorId(discussion.getAuthor().getId())
                .authorUsername(discussion.getAuthor().getUsername())
                .title(discussion.getTitle())
                .body(discussion.getBody())
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .build();
    }
}
