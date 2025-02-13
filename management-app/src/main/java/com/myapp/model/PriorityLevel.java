package com.myapp.model;

public class PriorityLevel {
    private String name;

    public PriorityLevel(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
