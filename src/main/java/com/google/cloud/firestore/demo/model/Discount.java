package com.google.cloud.firestore.demo.model;

public class Discount {
    private Integer amount;
    private Boolean applied;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }
}
