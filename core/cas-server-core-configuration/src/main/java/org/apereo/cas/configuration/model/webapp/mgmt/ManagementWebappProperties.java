package org.apereo.cas.configuration.model.webapp.mgmt;

import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
    private String defaultLocale = "en";
    private String authzAttributes;
    
    @NestedConfigurationProperty
    private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();

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

    public LdapAuthorizationProperties getLdapAuthz() {
        return ldapAuthz;
    }

    public void setLdapAuthz(final LdapAuthorizationProperties ldapAuthz) {
        this.ldapAuthz = ldapAuthz;
    }

    public String getAuthzAttributes() {
        return authzAttributes;
    }

    public void setAuthzAttributes(final String authzAttributes) {
        this.authzAttributes = authzAttributes;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(final String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}



