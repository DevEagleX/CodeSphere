package com.devcollab.service;

import com.devcollab.dto.AddTagRequest;
import com.devcollab.dto.TagResponse;
import com.devcollab.entity.Tag;
import com.devcollab.entity.TaggedItem;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.repository.TagRepository;
import com.devcollab.repository.TaggedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaggedItemRepository taggedItemRepository;
    private final WorkspaceService workspaceService;

    public List<TagResponse> listTags(String username, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, username);
        return tagRepository.findByWorkspaceId(workspaceId).stream()
                .map(t -> TagResponse.builder().id(t.getId()).name(t.getName()).build())
                .toList();
    }

    @Transactional
    public TagResponse tagItem(String username, UUID workspaceId, TaggedItem.EntityType entityType,
                                UUID entityId, AddTagRequest request) {
        workspaceService.requireMembership(workspaceId, username);

        // Find-or-create the tag itself (tags are workspace-scoped and reused across items).
        Tag tag = tagRepository.findByWorkspaceIdAndNameIgnoreCase(workspaceId, request.getName().trim())
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .workspace(workspaceService.findWorkspace(workspaceId))
                        .name(request.getName().trim())
                        .build()));

        if (!taggedItemRepository.existsByTagIdAndEntityTypeAndEntityId(tag.getId(), entityType, entityId)) {
            TaggedItem taggedItem = TaggedItem.builder()
                    .tag(tag)
                    .entityType(entityType)
                    .entityId(entityId)
                    .build();
            taggedItemRepository.save(taggedItem);
        }

        return TagResponse.builder().id(tag.getId()).name(tag.getName()).build();
    }

    public List<TagResponse> listItemTags(String username, UUID workspaceId, TaggedItem.EntityType entityType,
                                          UUID entityId) {
        workspaceService.requireMembership(workspaceId, username);
        return taggedItemRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .filter(ti -> ti.getTag().getWorkspace().getId().equals(workspaceId))
                .map(ti -> TagResponse.builder().id(ti.getTag().getId()).name(ti.getTag().getName()).build())
                .toList();
    }

    public List<TagResponse> getTagsForItem(TaggedItem.EntityType entityType, UUID entityId) {
        return taggedItemRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(ti -> TagResponse.builder().id(ti.getTag().getId()).name(ti.getTag().getName()).build())
                .toList();
    }
}
