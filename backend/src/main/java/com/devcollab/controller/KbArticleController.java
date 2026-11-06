package com.devcollab.controller;

import com.devcollab.dto.ArticleResponse;
import com.devcollab.dto.CreateArticleRequest;
import com.devcollab.dto.PageResponse;
import com.devcollab.dto.UpdateArticleRequest;
import com.devcollab.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class KbArticleController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/api/workspaces/{workspaceId}/kb/articles")
    public ResponseEntity<ArticleResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                    @Valid @RequestBody CreateArticleRequest request) {
        ArticleResponse response = knowledgeBaseService.createArticle(auth.getName(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/kb/articles")
    public ResponseEntity<PageResponse<ArticleResponse>> list(Authentication auth, @PathVariable UUID workspaceId,
                                                                @RequestParam(required = false) UUID categoryId,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(knowledgeBaseService.listArticles(auth.getName(), workspaceId, categoryId, pageable));
    }

    @GetMapping("/api/kb/articles/{id}")
    public ResponseEntity<ArticleResponse> getOne(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(knowledgeBaseService.getArticle(auth.getName(), id));
    }

    @PutMapping("/api/kb/articles/{id}")
    public ResponseEntity<ArticleResponse> update(Authentication auth, @PathVariable UUID id,
                                                    @Valid @RequestBody UpdateArticleRequest request) {
        return ResponseEntity.ok(knowledgeBaseService.updateArticle(auth.getName(), id, request));
    }

    @DeleteMapping("/api/kb/articles/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        knowledgeBaseService.deleteArticle(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
