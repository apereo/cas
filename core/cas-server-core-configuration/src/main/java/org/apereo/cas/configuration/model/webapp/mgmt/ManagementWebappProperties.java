package org.apereo.cas.configuration.model.webapp.mgmt;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ManagementWebappProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ManagementWebappProperties {
    private List<String> adminRoles = Arrays.asList("ROLE_ADMIN");

    /**
     * The server name/address of the management web application.
     */
    private String serverName = "https://localhost:8443";
    /**
     * Default locale to use when displaying UI components and views.
     */
    private String defaultLocale = "en";

    /**
     * Collection of attributes the authorized user must have in order to authenticate into the app.
     * Th attribute value(s) must match the expected role. To permit everything, you may use <code>*</code>.
     */
    private List<String> authzAttributes = new ArrayList<>();

    /**
     * Control authorization and access into the app via LDAP directly.
     */
    private Ldap ldap = new Ldap();

    /**
     * Location of the resource that contains the authorized accounts.
     * This file lists the set of users that are allowed access to the CAS sensitive/admin endpoints.
     * The syntax of each entry should be in the form of:
     * <code>username=notused,grantedAuthority[,grantedAuthority][,enabled|disabled]</code>
     */
    private Resource userPropertiesFile = new ClassPathResource("user-details.properties");

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public List<String> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(final List<String> adminRoles) {
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

    public List<String> getAuthzAttributes() {
        return authzAttributes;
    }

    public void setAuthzAttributes(final List<String> authzAttributes) {
        this.authzAttributes = authzAttributes;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(final String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -8129280052479631538L;
        /**
         * Defines authorization settings that allow access to the app via LDAP.
         */
        @NestedConfigurationProperty
        private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();

        public LdapAuthorizationProperties getLdapAuthz() {
            return ldapAuthz;
        }

        public void setLdapAuthz(final LdapAuthorizationProperties ldapAuthz) {
            this.ldapAuthz = ldapAuthz;
        }
    }
}



