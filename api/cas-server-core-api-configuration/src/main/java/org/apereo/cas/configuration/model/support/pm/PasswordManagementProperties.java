package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
public class PasswordManagementProperties implements Serializable {

    private static final long serialVersionUID = -260644582798411176L;
    /**
     * Flag to indicate if password management facility is enabled.
     */
    private boolean enabled;

    /**
     * Flag to indicate whether successful password change should trigger login automatically.
     */
    private boolean autoLogin;

    /**
     * A String value representing password policy regex pattarn.
     * <p>
     * Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character.
     */
    private String policyPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}";

    /**
     * Manage account passwords in LDAP.
     */
    private Ldap ldap = new Ldap();
    /**
     * Manage account passwords in database.
     */
    private Jdbc jdbc = new Jdbc();
    /**
     * Manage account passwords via REST.
     */
    private Rest rest = new Rest();
    /**
     * Manage account passwords in JSON resources.
     */
    private Json json = new Json();

    /**
     * Settings related to resetting password.
     */
    private Reset reset = new Reset();

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

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

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(final boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public String getPolicyPattern() {
        return policyPattern;
    }

    public void setPolicyPattern(final String policyPattern) {
        this.policyPattern = policyPattern;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    @RequiresModule(name = "cas-server-support-pm-jdbc")
    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = 4746591112640513465L;

        /**
         * Password encoder properties.
         */
        @NestedConfigurationProperty
        private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

        /**
         * SQL query to change the password and update.
         */
        private String sqlChangePassword;
        /**
         * SQL query to locate the user email address.
         */
        private String sqlFindEmail;
        /**
         * SQL query to locate security questions for the account, if any.
         */
        private String sqlSecurityQuestions;

        public String getSqlChangePassword() {
            return sqlChangePassword;
        }

        public void setSqlChangePassword(final String sqlChangePassword) {
            this.sqlChangePassword = sqlChangePassword;
        }

        public String getSqlFindEmail() {
            return sqlFindEmail;
        }

        public void setSqlFindEmail(final String sqlFindEmail) {
            this.sqlFindEmail = sqlFindEmail;
        }

        public String getSqlSecurityQuestions() {
            return sqlSecurityQuestions;
        }

        public void setSqlSecurityQuestions(final String sqlSecurityQuestions) {
            this.sqlSecurityQuestions = sqlSecurityQuestions;
        }

        public PasswordEncoderProperties getPasswordEncoder() {
            return passwordEncoder;
        }

        public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }
    }

    @RequiresModule(name = "cas-server-support-pm-rest")
    public static class Rest implements Serializable {
        private static final long serialVersionUID = 5262948164099973872L;
        /**
         * Endpoint URL to use when locating email addresses.
         */
        private String endpointUrlEmail;
        /**
         * Endpoint URL to use when locating security questions.
         */
        private String endpointUrlSecurityQuestions;
        /**
         * Endpoint URL to use when updating passwords..
         */
        private String endpointUrlChange;

        public String getEndpointUrlEmail() {
            return endpointUrlEmail;
        }

        public void setEndpointUrlEmail(final String endpointUrlEmail) {
            this.endpointUrlEmail = endpointUrlEmail;
        }

        public String getEndpointUrlSecurityQuestions() {
            return endpointUrlSecurityQuestions;
        }

        public void setEndpointUrlSecurityQuestions(final String endpointUrlSecurityQuestions) {
            this.endpointUrlSecurityQuestions = endpointUrlSecurityQuestions;
        }

        public String getEndpointUrlChange() {
            return endpointUrlChange;
        }

        public void setEndpointUrlChange(final String endpointUrlChange) {
            this.endpointUrlChange = endpointUrlChange;
        }
    }

    @RequiresModule(name = "cas-server-support-pm-ldap")
    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -2610186056194686825L;
        /**
         * Collection of attribute names that indicate security questions answers.
         * This is done via a key-value structure where the key is the attribute name
         * for the security question and the value is the attribute name for the answer linked to the question.
         */
        private Map<String, String> securityQuestionsAttributes = new LinkedHashMap<>();
        /**
         * Base DN to start the search and update operations.
         */
        private String baseDn;
        /**
         * User filter to start the search.
         */
        private String userFilter;
        /**
         * The specific variant of LDAP
         * based on which update operations will be constructed.
         */
        private LdapType type = LdapType.AD;

        public Map<String, String> getSecurityQuestionsAttributes() {
            return securityQuestionsAttributes;
        }

        public void setSecurityQuestionsAttributes(final Map<String, String> s) {
            this.securityQuestionsAttributes = s;
        }

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

    @RequiresModule(name = "cas-server-support-pm-webflow")
    public static class Reset implements Serializable {
        private static final long serialVersionUID = 3453970349530670459L;
        /**
         * Crypto settings on how to reset the password.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        /**
         * Text one might receive as a notification to reset the password.
         */
        private String text = "Reset your password via this link: %s";
        /**
         * The subject of the notification for password resets.
         */
        private String subject = "Password Reset";
        /**
         * From address of the notification.
         */
        private String from;
        /**
         * Attribute indicating the an email address where notification is sent.
         */
        private String emailAttribute = "mail";
        /**
         * Whether reset operations require security questions,
         * or should they be marked as optional.
         */
        private boolean securityQuestionsEnabled = true;

        /**
         * How long in minutes should the password expiration link remain valid.
         */
        private float expirationMinutes = 1;

        public Reset() {
        }

        public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
            return crypto;
        }

        public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
            this.crypto = crypto;
        }

        public String getEmailAttribute() {
            return emailAttribute;
        }

        public void setEmailAttribute(final String emailAttribute) {
            this.emailAttribute = emailAttribute;
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

        public boolean isSecurityQuestionsEnabled() {
            return securityQuestionsEnabled;
        }

        public void setSecurityQuestionsEnabled(final boolean securityQuestionsEnabled) {
            this.securityQuestionsEnabled = securityQuestionsEnabled;
        }
    }

    @RequiresModule(name = "cas-server-support-pm")
    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 1129426669588789974L;
    }
}
