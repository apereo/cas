package org.apereo.cas.configuration.model.support.pm;

import com.google.common.collect.Maps;
import org.apereo.cas.configuration.model.core.ticket.SigningEncryptionProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

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
    private Reset reset = new Reset();

    public Reset getReset() {
        return reset;
    }

    public void setReset(final Reset reset) {
        this.reset = reset;
    }

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
             * Active directory.
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
    
    public static class Reset {
        @NestedConfigurationProperty
        private SigningEncryptionProperties security = new SigningEncryptionProperties();
        
        private String text = "Reset your password via this link: %s";
        private String subject = "Password Reset";
        private String from;
        private String emailAttribute = "mail";
        private Map<String, String> securityQuestionsAttributes = Maps.newLinkedHashMap();
        
        private float expirationMinutes = 1;

        public Reset() {
            security.setCipherEnabled(true);
        }

        public SigningEncryptionProperties getSecurity() {
            return security;
        }

        public String getEmailAttribute() {
            return emailAttribute;
        }

        public void setEmailAttribute(final String emailAttribute) {
            this.emailAttribute = emailAttribute;
        }

        public void setSecurity(final SigningEncryptionProperties security) {
            this.security = security;
        }

        public Map<String, String> getSecurityQuestionsAttributes() {
            return securityQuestionsAttributes;
        }

        public void setSecurityQuestionsAttributes(final Map<String, String> s) {
            this.securityQuestionsAttributes = s;
        }

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(final String subject) {
            this.subject = subject;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(final String from) {
            this.from = from;
        }

        public float getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(final float expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}
