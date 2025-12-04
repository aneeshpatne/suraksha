package com.aneesh.suraksha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppSecretConfig {

    @Value("${app.secret.key}")
    private String secretKey;
    @Value("${app.secret_refresh.key}")
    private String secretRefreshKey;

    public String getSecretKey() {
        return secretKey;
    }

    public String getRefreshSecretKey() {
        return secretRefreshKey;
    }

}
