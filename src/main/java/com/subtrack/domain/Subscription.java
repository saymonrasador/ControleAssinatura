package com.subtrack.domain;

import java.time.LocalDate;

public class Subscription {

    private String id;
    private String userId;
    private String name;
    private double price;
    private Periodicity periodicity;
    private LocalDate purchaseDate;
    private LocalDate nextDueDate;
    private boolean autoRenew;
    private SubscriptionStatus status;
    private boolean isActive;
    private String categoryId;
    private String paymentMethodId;

    // Campos transitórios para exibição (não persistidos)
    private String categoryName;
    private String paymentMethodName;
    private String categoryColorHex;
    private String paymentMethodColorHex;

    public Subscription() {
    }

    public Subscription(String id, String userId, String name, double price,
            Periodicity periodicity, LocalDate purchaseDate,
            LocalDate nextDueDate, boolean autoRenew,
            SubscriptionStatus status, boolean isActive,
            String categoryId, String paymentMethodId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.price = price;
        this.periodicity = periodicity;
        this.purchaseDate = purchaseDate;
        this.nextDueDate = nextDueDate;
        this.autoRenew = autoRenew;
        this.status = status;
        this.isActive = isActive;
        this.categoryId = categoryId;
        this.paymentMethodId = paymentMethodId;
    }

    // --- Getters and Setters ---

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    // Campos transitórios para exibição
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getCategoryColorHex() {
        return categoryColorHex;
    }

    public void setCategoryColorHex(String categoryColorHex) {
        this.categoryColorHex = categoryColorHex;
    }

    public String getPaymentMethodColorHex() {
        return paymentMethodColorHex;
    }

    public void setPaymentMethodColorHex(String paymentMethodColorHex) {
        this.paymentMethodColorHex = paymentMethodColorHex;
    }

    /**
     * Retorna o custo mensal efetivo desta assinatura.
     * Assinaturas mensais retornam o preço cheio; assinaturas anuais retornam o
     * preço / 12.
     */
    public double getMonthlyEquivalent() {
        return periodicity == Periodicity.ANUAL ? price / 12.0 : price;
    }
}
