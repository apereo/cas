package org.apereo.cas.configuration.model.support.aup;

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

    public static class Ldap {
        private String searchFilter = "cn={0}";

        private String baseDn = "dc=example,dc=org";

        public String getSearchFilter() {
            return searchFilter;
        }

        public void setSearchFilter(final String searchFilter) {
            this.searchFilter = searchFilter;
        }

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }
    }
    
}
