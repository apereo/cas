package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;

/**
 * This is {@link AcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class AcceptableUsagePolicyProperties {
    private Ldap ldap = new Ldap();

    private String aupAttributeName = "aupAccepted";

    public String getAupAttributeName() {
        return aupAttributeName;
    }

    public void setAupAttributeName(final String aupAttributeName) {
        this.aupAttributeName = aupAttributeName;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class Ldap extends AbstractLdapProperties {
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
    }
    
}
