package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthorizationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * This is {@link AdminPagesSecurityProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AdminPagesSecurityProperties {
    private String ip = "a^";
    private List<String> adminRoles = Arrays.asList("ROLE_ADMIN", "ROLE_ACTUATOR");
    private String loginUrl;
    private String service;
    private Resource users;
    private boolean actuatorEndpointsEnabled;

    private Jdbc jdbc = new Jdbc();
    private Ldap ldap = new Ldap();
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

    public class Jaas {
        private Resource loginConfig;
        private boolean refreshConfigurationOnStartup = true;
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
    
    public class Ldap extends AbstractLdapAuthenticationProperties {
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

    public class Jdbc extends AbstractJpaProperties {
        private String rolePrefix;
        private String query;

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
