package org.apereo.cas.adaptors.authy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("okta.mfa.cas")
public class OktaMfaProperties {
    private String token;
    private String url;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
