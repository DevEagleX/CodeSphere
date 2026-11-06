package com.devcollab.controller;

import com.devcollab.dto.NotificationResponse;
import com.devcollab.dto.PageResponse;
import com.devcollab.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(Authentication auth,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.listMine(auth.getName(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication auth) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(auth.getName())));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(Authentication auth, @PathVariable UUID id) {
        notificationService.markRead(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
