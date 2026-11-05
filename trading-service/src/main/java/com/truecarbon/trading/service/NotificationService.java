package com.truecarbon.trading.service;

import com.truecarbon.trading.domain.*;
import com.truecarbon.trading.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.*;

// ── NotificationService ──────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notifRepo;
    private final SimpMessagingTemplate  ws;

    @Transactional
    public void notify(UUID userId, String message, String type, UUID refId) {
        Notification n = Notification.builder()
            .userId(userId).message(message).type(type).refId(refId).build();
        notifRepo.save(n);
        try {
            ws.convertAndSend("/topic/notifications/" + userId,
                Map.of("id", n.getId().toString(), "message", message, "type", type));
        } catch (Exception e) {
            log.debug("WebSocket push failed for {}: {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getForUser(UUID userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notifRepo.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notifRepo.markAllRead(userId);
    }
}
