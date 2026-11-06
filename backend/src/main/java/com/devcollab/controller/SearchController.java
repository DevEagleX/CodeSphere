package com.devcollab.controller;

import com.devcollab.dto.SearchResultResponse;
import com.devcollab.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/workspaces/{workspaceId}/search")
    public ResponseEntity<List<SearchResultResponse>> search(Authentication auth, @PathVariable UUID workspaceId,
                                                               @RequestParam String q) {
        return ResponseEntity.ok(searchService.search(auth.getName(), workspaceId, q));
    }
}
