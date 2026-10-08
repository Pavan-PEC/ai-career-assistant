package com.pavan.ai_service.event;

import java.time.Instant;

public record NotificationEvent(
        Long userId,
        String type,
        String message,
        Instant createdAt
) {
}
