package com.subtrack.domain;

import java.time.LocalDateTime;

public class Notification {

    private String id;
    private String userId;
    private String title;
    private String message;
    private boolean isRead;
    private String subscriptionId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {
    }

    public Notification(String id, String userId, String title, String message,
            boolean isRead, String subscriptionId,
            LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.subscriptionId = subscriptionId;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
