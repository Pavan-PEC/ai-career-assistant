package com.pavan.notification_service.event;

import java.time.Instant;

public record NotificationEvent(
        Long userId,
        String type,
        String message,
        Instant createdAt
) {
}
