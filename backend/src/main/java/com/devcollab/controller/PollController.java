package com.devcollab.controller;

import com.devcollab.dto.CreatePollRequest;
import com.devcollab.dto.PollResponse;
import com.devcollab.service.PollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping("/api/workspaces/{workspaceId}/channels/{channelId}/polls")
    public ResponseEntity<PollResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                @PathVariable UUID channelId,
                                                @Valid @RequestBody CreatePollRequest request) {
        PollResponse response = pollService.create(auth.getName(), workspaceId, channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/channels/{channelId}/polls")
    public ResponseEntity<List<PollResponse>> list(Authentication auth, @PathVariable UUID workspaceId,
                                                     @PathVariable UUID channelId) {
        return ResponseEntity.ok(pollService.list(auth.getName(), workspaceId, channelId));
    }

    @PostMapping("/api/polls/{pollId}/options/{optionId}/vote")
    public ResponseEntity<PollResponse> vote(Authentication auth, @PathVariable UUID pollId,
                                              @PathVariable UUID optionId) {
        return ResponseEntity.ok(pollService.vote(auth.getName(), pollId, optionId));
    }

    @PostMapping("/api/polls/{pollId}/close")
    public ResponseEntity<Void> close(Authentication auth, @PathVariable UUID pollId) {
        pollService.close(auth.getName(), pollId);
        return ResponseEntity.noContent().build();
    }
}
