package com.logistics.tracking.ports;

/** Outbound port owned by Tracking & Notification. */
public interface NotificationPort {
    void send(NotificationMessage message);

    record NotificationMessage(String recipient, String subject, String body) {}
}
