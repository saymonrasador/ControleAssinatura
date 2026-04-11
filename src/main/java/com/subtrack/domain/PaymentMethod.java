package com.subtrack.domain;

public class PaymentMethod {

    private String id;
    private String userId;
    private String name;
    private String colorHex;
    private boolean isDefault;

    public PaymentMethod() {
    }

    public PaymentMethod(String id, String userId, String name, String colorHex, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.colorHex = colorHex;
        this.isDefault = isDefault;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return name;
    }
}
