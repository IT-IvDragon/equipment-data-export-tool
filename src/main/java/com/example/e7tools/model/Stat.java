package com.example.e7tools.model;

public class Stat {
    private String type;

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public Integer getRolls() {
        return rolls;
    }

    public Boolean getModified() {
        return modified;
    }

    private double value;
    private Integer rolls;
    private Boolean modified;

    public Stat(String type, double value, Integer rolls, Boolean modified) {
        this.type = type;
        this.value = value;
        this.rolls = rolls;
        this.modified = modified;
    }

    public Stat(String type, double value) {
        this.type = type;
        this.value = value;
    }

    // getters and setters

    @Override
    public String toString() {
        return "Stat{" +
                "type='" + type + '\'' +
                ", value=" + value +
                ", rolls=" + rolls +
                ", modified=" + modified +
                '}';
    }
}