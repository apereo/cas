package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;

/**
 * This is {@link PasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordManagementProperties {
    private boolean enabled;
    
    // Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character
    private String policyPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}";
    
    private Ldap ldap = new Ldap();
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPolicyPattern() {
        return policyPattern;
    }

    public void setPolicyPattern(final String policyPattern) {
        this.policyPattern = policyPattern;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class Ldap extends AbstractLdapProperties {

        /**
         * The ldap type used to handle specific ops.
         */
        public enum LdapType {
            /**
             * Generic ldap type (openldap, 389ds, etc).
             */
            GENERIC,
            /**
             * Active directory
             */
            AD
        }
        private String baseDn;
        private String userFilter;
        private LdapType type = LdapType.AD;
        
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

        public LdapType getType() {
            return type;
        }

        public void setType(final LdapType type) {
            this.type = type;
        }
    }
}
