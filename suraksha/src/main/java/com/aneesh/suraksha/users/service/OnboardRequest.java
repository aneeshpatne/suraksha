package com.aneesh.suraksha.users.service;

public class OnboardRequest {
    public String name;

    public OnboardRequest(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
