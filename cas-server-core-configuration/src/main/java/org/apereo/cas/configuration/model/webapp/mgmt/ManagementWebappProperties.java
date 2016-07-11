package org.apereo.cas.configuration.model.webapp.mgmt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link ManagementWebappProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ManagementWebappProperties {
    private String adminRoles = "ROLE_ADMIN";
    private String serverName = "https://localhost:8443";
    
    private Resource userPropertiesFile = new ClassPathResource("user-details.properties");
    
    public String getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(final String adminRoles) {
        this.adminRoles = adminRoles;
    }

    public Resource getUserPropertiesFile() {
        return userPropertiesFile;
    }

    public void setUserPropertiesFile(final Resource userPropertiesFile) {
        this.userPropertiesFile = userPropertiesFile;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }
}



