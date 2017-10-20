package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link AdminPagesSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class AdminPagesSecurityProperties implements Serializable {
    private static final long serialVersionUID = 9129787932447507179L;
    /**
     * The IP address pattern that can control access to the admin status endpoints.
     */
    private String ip = "a^";

    /**
     * Roles that are required for access to the admin status endpoint
     * in the event that access is controlled via external authentication
     * means such as Spring Security's authentication providers.
     */
    private List<String> adminRoles = CollectionUtils.wrapList("ROLE_ADMIN", "ROLE_ACTUATOR");

    /**
     * CAS server login URL to use. 
     * When defined, will begin to protect the access status endpoints via CAS itself.
     */
    private String loginUrl;
    /**
     * The service parameter for the admin status endpoint. 
     * This is typically set to the dashboard url as the initial starting point
     * for the redirect.
     */
    private String service;

    /**
     * List of users allowed access to the admin status endpoint
     * provided CAS is controlling access to the status endpoint.
     * If you decide to protect other administrative endpoints via CAS itself, 
     * you will need to provide a reference to the list of authorized users in the CAS configuration. 
     */
    private Resource users;

    /**
     * Whether Spring Boot's actuator endpoints should show up on the dashboard.
     */
    private boolean actuatorEndpointsEnabled;

    /**
     * Enable Spring Security's JDBC authentication provider
     * for admin status authorization and access control.
     */
    private Jdbc jdbc = new Jdbc();
    /**
     * Enable Spring Security's LDAP authentication provider
     * for admin status authorization and access control.
     */
    private Ldap ldap = new Ldap();

    /**
     * Enable Spring Security's JAAS authentication provider
     * for admin status authorization and access control.
     */
    private Jaas jaas = new Jaas();

    public Jaas getJaas() {
        return jaas;
    }

    public void setJaas(final Jaas jaas) {
        this.jaas = jaas;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

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

    public List<String> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(final List<String> adminRoles) {
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

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class Jaas implements Serializable {
        private static final long serialVersionUID = -3024678577827371641L;
        /**
         * JAAS login resource file.
         */
        private Resource loginConfig;
        /**
         * If set, a call to {@code Configuration#refresh()} 
         * will be made by {@code #configureJaas(Resource)} method.
         */
        private boolean refreshConfigurationOnStartup = true;

        /**
         * The login context name should coincide with a given index in the login config specified.
         * This name is used as the index to the configuration specified in the login config property.
         * 
<pre>
JAASTest {
    org.springframework.security.authentication.jaas.TestLoginModule required;
};
</pre>
         In the above example, {@code JAASTest} should be set as the context name.
         */
        private String loginContextName;

        public Resource getLoginConfig() {
            return loginConfig;
        }

        public void setLoginConfig(final Resource loginConfig) {
            this.loginConfig = loginConfig;
        }

        public boolean isRefreshConfigurationOnStartup() {
            return refreshConfigurationOnStartup;
        }

        public void setRefreshConfigurationOnStartup(final boolean refreshConfigurationOnStartup) {
            this.refreshConfigurationOnStartup = refreshConfigurationOnStartup;
        }

        public String getLoginContextName() {
            return loginContextName;
        }

        public void setLoginContextName(final String loginContextName) {
            this.loginContextName = loginContextName;
        }
    }
    
    public static class Ldap extends AbstractLdapAuthenticationProperties {
        private static final long serialVersionUID = -7333244539096172557L;

        /**
         * Control authorization settings via LDAP
         * after ldap authentication.
         */
        @NestedConfigurationProperty
        private LdapAuthorizationProperties ldapAuthz = new LdapAuthorizationProperties();

        /**
         * Gets ldap authz.
         *
         * @return the ldap authz
         */
        public LdapAuthorizationProperties getLdapAuthz() {
            ldapAuthz.setBaseDn(getBaseDn());
            ldapAuthz.setSearchFilter(getUserFilter());
            return ldapAuthz;
        }

        public void setLdapAuthz(final LdapAuthorizationProperties ldapAuthz) {
            this.ldapAuthz = ldapAuthz;
        }
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 2625666117528467867L;

        /**
         * Prefix to add to the role.
         */
        private String rolePrefix;

        /**
         * Query to execute in order to authenticate users via JDBC.
         * Example:
         * {@code SELECT username,password,enabled FROM users WHERE username=?}
         */
        private String query;
        
        /**
         * Password encoder properties.
         */
        @NestedConfigurationProperty
        private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

        public String getRolePrefix() {
            return rolePrefix;
        }

        public void setRolePrefix(final String rolePrefix) {
            this.rolePrefix = rolePrefix;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(final String query) {
            this.query = query;
        }

        public PasswordEncoderProperties getPasswordEncoder() {
            return passwordEncoder;
        }

        public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }
    }
}
