package org.apereo.cas.configuration.model.webapp.mgmt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link ManagementWebappProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas-management", ignoreUnknownFields = false)
public class ManagementWebappProperties {
    private String defaultServiceUrl;
    private String adminRoles;
    private String loginUrl;
    private Resource userPropertiesFile = new ClassPathResource("user-details.properties");

    public String getDefaultServiceUrl() {
        return defaultServiceUrl;
    }

    public void setDefaultServiceUrl(final String defaultServiceUrl) {
        this.defaultServiceUrl = defaultServiceUrl;
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

    public Resource getUserPropertiesFile() {
        return userPropertiesFile;
    }

    public void setUserPropertiesFile(final Resource userPropertiesFile) {
        this.userPropertiesFile = userPropertiesFile;
    }
}



