package com.devcollab.controller;

import com.devcollab.dto.*;
import com.devcollab.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ChannelResponse> create(Authentication auth, @PathVariable UUID workspaceId,
                                                    @Valid @RequestBody CreateChannelRequest request) {
        ChannelResponse response = channelService.createChannel(auth.getName(), workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> list(Authentication auth, @PathVariable UUID workspaceId) {
        return ResponseEntity.ok(channelService.listChannels(auth.getName(), workspaceId));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> getOne(Authentication auth, @PathVariable UUID workspaceId,
                                                    @PathVariable UUID channelId) {
        return ResponseEntity.ok(channelService.getChannel(auth.getName(), workspaceId, channelId));
    }

    @PostMapping("/{channelId}/members")
    public ResponseEntity<ChannelMemberResponse> addMember(Authentication auth, @PathVariable UUID workspaceId,
                                                             @PathVariable UUID channelId,
                                                             @Valid @RequestBody AddChannelMemberRequest request) {
        ChannelMemberResponse response = channelService.addMember(auth.getName(), workspaceId, channelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID workspaceId,
                                        @PathVariable UUID channelId) {
        channelService.deleteChannel(auth.getName(), workspaceId, channelId);
        return ResponseEntity.noContent().build();
    }
}
