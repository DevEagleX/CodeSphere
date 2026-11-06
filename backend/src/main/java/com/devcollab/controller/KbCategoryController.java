package com.devcollab.controller;

import com.devcollab.dto.CategoryResponse;
import com.devcollab.dto.CreateCategoryRequest;
import com.devcollab.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/kb/categories")
@RequiredArgsConstructor
public class KbCategoryController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                     @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = knowledgeBaseService.createCategory(auth.getName(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(knowledgeBaseService.listCategories(auth.getName(), workspaceId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID workspaceId,
                                        @PathVariable UUID categoryId) {
        knowledgeBaseService.deleteCategory(auth.getName(), workspaceId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
