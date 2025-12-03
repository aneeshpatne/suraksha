package com.aneesh.suraksha.users.service;

public class OnboardResponse {
    private String id;
    private String name;
    private String apiKey;

    public OnboardResponse(String id, String name, String apiKey) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
