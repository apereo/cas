package org.apereo.cas.configuration.model.core.web.security;

import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link AdminPagesSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdminPagesSecurityProperties {

    private String ip = "127\\.0\\.0\\.1|0:0:0:0:0:0:0:1";
    private Set<String> adminRoles = new HashSet<>(Collections.singletonList("ROLE_ADMIN"));
    private String loginUrl;
    private String service;
    private Resource users;
    private boolean actuatorEndpointsEnabled;

    public boolean isActuatorEndpointsEnabled() {
        return actuatorEndpointsEnabled;
    }

    public void setActuatorEndpointsEnabled(final boolean actuatorEndpointsEnabled) {
        this.actuatorEndpointsEnabled = actuatorEndpointsEnabled;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public Set<String> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(final Set<String> adminRoles) {
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
