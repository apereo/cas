package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link PrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrincipalAttributesProperties {

    private int expireInMinutes = 30;
    private int maximumCacheSize = 10000;

    private Map<String, String> attributes = new HashMap();

    private Jdbc jdbc = new Jdbc();

    private Ldap ldap = new Ldap();

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public int getExpireInMinutes() {
        return expireInMinutes;
    }

    public void setExpireInMinutes(final int expireInMinutes) {
        this.expireInMinutes = expireInMinutes;
    }

    public int getMaximumCacheSize() {
        return maximumCacheSize;
    }

    public void setMaximumCacheSize(final int maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public static class Jdbc extends AbstractJpaProperties {
        private String sql;

        private String username;

        public String getSql() {
            return sql;
        }

        public void setSql(final String sql) {
            this.sql = sql;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }
    }

    public static class Ldap extends AbstractLdapProperties {
        private boolean subtreeSearch = true;
        private String baseDn;
        private String userFilter;

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getUserFilter() {
            return userFilter;
        }

        public void setUserFilter(final String userFilter) {
            this.userFilter = userFilter;
        }

        public boolean isSubtreeSearch() {
            return subtreeSearch;
        }

        public void setSubtreeSearch(final boolean subtreeSearch) {
            this.subtreeSearch = subtreeSearch;
        }
    }
}
