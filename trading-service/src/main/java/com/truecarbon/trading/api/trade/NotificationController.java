package com.truecarbon.trading.api.trade;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.truecarbon.trading.api.Dtos.NotificationResponse;
import com.truecarbon.trading.security.TradingUserPrincipal;
import com.truecarbon.trading.service.NotificationService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
class NotificationController {

    private final NotificationService notifService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        List<NotificationResponse> result = notifService.getForUser(principal.getUserId())
            .stream()
            .map(n -> new NotificationResponse(
                n.getId(), n.getMessage(), n.getType(),
                n.isRead(), n.getRefId(), n.getCreatedAt()
            ))
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        return ResponseEntity.ok(
            Map.of("count", notifService.countUnread(principal.getUserId())));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        notifService.markAllRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
