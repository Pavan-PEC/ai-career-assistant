package com.pavan.notification_service.listener;

import com.pavan.notification_service.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = "${app.rabbitmq.notification-queue}")
    public void handleNotification(NotificationEvent event) {
        log.info("Notification for user {} [{}]: {}", event.userId(), event.type(), event.message());
    }
}
