package com.devcollab.service;

import com.devcollab.dto.SearchResultResponse;
import com.devcollab.entity.Discussion;
import com.devcollab.entity.Document;
import com.devcollab.entity.KbArticle;
import com.devcollab.repository.DiscussionRepository;
import com.devcollab.repository.DocumentRepository;
import com.devcollab.repository.KbArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int SNIPPET_LENGTH = 160;

    private final DiscussionRepository discussionRepository;
    private final KbArticleRepository kbArticleRepository;
    private final DocumentRepository documentRepository;
    private final WorkspaceService workspaceService;

    // Fans out across the three searchable content types in one call. Each is a
    // separate simple query (see repository search methods) rather than one giant
    // UNION query - keeps each query readable and lets each content type evolve
    // its own search relevance rules independently later.
    public List<SearchResultResponse> search(String username, UUID workspaceId, String keyword) {
        workspaceService.requireMembership(workspaceId, username);

        List<SearchResultResponse> results = new ArrayList<>();

        for (Discussion d : discussionRepository.searchByWorkspace(workspaceId, keyword)) {
            results.add(SearchResultResponse.builder()
                    .entityType("DISCUSSION")
                    .entityId(d.getId())
                    .title(d.getTitle())
                    .snippet(snippet(d.getBody()))
                    .createdAt(d.getCreatedAt())
                    .build());
        }

        for (KbArticle a : kbArticleRepository.searchByWorkspace(workspaceId, keyword)) {
            results.add(SearchResultResponse.builder()
                    .entityType("KB_ARTICLE")
                    .entityId(a.getId())
                    .title(a.getTitle())
                    .snippet(snippet(a.getBody()))
                    .createdAt(a.getCreatedAt())
                    .build());
        }

        for (Document doc : documentRepository.searchByWorkspace(workspaceId, keyword)) {
            results.add(SearchResultResponse.builder()
                    .entityType("DOCUMENT")
                    .entityId(doc.getId())
                    .title(doc.getTitle())
                    .snippet(snippet(doc.getBody()))
                    .createdAt(doc.getCreatedAt())
                    .build());
        }

        return results;
    }

    private String snippet(String body) {
        if (body == null) return "";
        return body.length() > SNIPPET_LENGTH ? body.substring(0, SNIPPET_LENGTH) + "..." : body;
    }
}
