package com.subtrack.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentRecord {

    private String id;
    private String subscriptionId;
    private LocalDate paymentDate;
    private double amount;
    private String competence; // Identificador do ciclo, ex: "2026-04"
    private String subscriptionNameSnapshot;
    private String categoryNameSnapshot;
    private String paymentMethodNameSnapshot;
    private LocalDateTime createdAt;

    public PaymentRecord() {
    }

    public PaymentRecord(String id, String subscriptionId, LocalDate paymentDate,
            double amount, String competence,
            String subscriptionNameSnapshot,
            String categoryNameSnapshot,
            String paymentMethodNameSnapshot,
            LocalDateTime createdAt) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.competence = competence;
        this.subscriptionNameSnapshot = subscriptionNameSnapshot;
        this.categoryNameSnapshot = categoryNameSnapshot;
        this.paymentMethodNameSnapshot = paymentMethodNameSnapshot;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    public String getSubscriptionNameSnapshot() {
        return subscriptionNameSnapshot;
    }

    public void setSubscriptionNameSnapshot(String subscriptionNameSnapshot) {
        this.subscriptionNameSnapshot = subscriptionNameSnapshot;
    }

    public String getCategoryNameSnapshot() {
        return categoryNameSnapshot;
    }

    public void setCategoryNameSnapshot(String categoryNameSnapshot) {
        this.categoryNameSnapshot = categoryNameSnapshot;
    }

    public String getPaymentMethodNameSnapshot() {
        return paymentMethodNameSnapshot;
    }

    public void setPaymentMethodNameSnapshot(String paymentMethodNameSnapshot) {
        this.paymentMethodNameSnapshot = paymentMethodNameSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
