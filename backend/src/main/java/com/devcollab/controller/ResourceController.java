package com.devcollab.controller;

import com.devcollab.dto.ResourceResponse;
import com.devcollab.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceResponse> upload(Authentication auth, @PathVariable UUID workspaceId,
                                                     @RequestParam("file") MultipartFile file) {
        ResourceResponse response = resourceService.upload(auth.getName(), workspaceId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> list(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(resourceService.list(auth.getName(), workspaceId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(Authentication auth, @PathVariable UUID workspaceId,
                                              @PathVariable UUID id) {
        com.devcollab.entity.Resource metadata = resourceService.getMetadata(auth.getName(), workspaceId, id);
        Resource fileResource = resourceService.download(auth.getName(), workspaceId, id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(fileResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID workspaceId, @PathVariable UUID id) {
        resourceService.delete(auth.getName(), workspaceId, id);
        return ResponseEntity.noContent().build();
    }
}
