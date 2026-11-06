package com.devcollab.mapper;

import com.devcollab.dto.ReplyResponse;
import com.devcollab.entity.Reply;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReplyMapper {

    public ReplyResponse toFlatResponse(Reply reply) {
        return ReplyResponse.builder()
                .id(reply.getId())
                .discussionId(reply.getDiscussion().getId())
                .parentReplyId(reply.getParentReply() != null ? reply.getParentReply().getId() : null)
                .authorId(reply.getAuthor().getId())
                .authorUsername(reply.getAuthor().getUsername())
                .body(reply.getBody())
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }

    public List<ReplyResponse> toTree(List<Reply> flatReplies) {
        Map<UUID, ReplyResponse> byId = new LinkedHashMap<>();
        for (Reply reply : flatReplies) {
            byId.put(reply.getId(), toFlatResponse(reply));
        }

        List<ReplyResponse> roots = new ArrayList<>();
        for (Reply reply : flatReplies) {
            ReplyResponse dto = byId.get(reply.getId());
            UUID parentId = reply.getParentReply() != null ? reply.getParentReply().getId() : null;

            if (parentId == null) {
                roots.add(dto);
            } else {
                ReplyResponse parentDto = byId.get(parentId);
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }

        return roots;
    }
}
