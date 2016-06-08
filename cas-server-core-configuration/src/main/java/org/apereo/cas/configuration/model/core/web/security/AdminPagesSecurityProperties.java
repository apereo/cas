package org.apereo.cas.configuration.model.core.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * This is {@link AdminPagesSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.securityContext.adminpages", ignoreUnknownFields = false)
public class AdminPagesSecurityProperties {

    private String ip = "127\\.0\\.0\\.1";
    private String adminRoles;
    private String loginUrl;
    private String service;
    private Resource users;

    public String getIp() {
        return ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public String getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(final String adminRoles) {
        this.adminRoles = adminRoles;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getService() {
        return service;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public Resource getUsers() {
        return users;
    }

    public void setUsers(final Resource users) {
        this.users = users;
    }
}
