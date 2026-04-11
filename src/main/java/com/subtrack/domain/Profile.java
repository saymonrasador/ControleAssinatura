package com.subtrack.domain;

public class Profile {

    private String id;
    private String userId;
    private int alertDaysBefore;
    private double monthlySpendingLimit;

    public Profile() {
    }

    public Profile(String id, String userId, int alertDaysBefore, double monthlySpendingLimit) {
        this.id = id;
        this.userId = userId;
        this.alertDaysBefore = alertDaysBefore;
        this.monthlySpendingLimit = monthlySpendingLimit;
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

    public int getAlertDaysBefore() {
        return alertDaysBefore;
    }

    public void setAlertDaysBefore(int alertDaysBefore) {
        this.alertDaysBefore = alertDaysBefore;
    }

    public double getMonthlySpendingLimit() {
        return monthlySpendingLimit;
    }

    public void setMonthlySpendingLimit(double monthlySpendingLimit) {
        this.monthlySpendingLimit = monthlySpendingLimit;
    }
}
