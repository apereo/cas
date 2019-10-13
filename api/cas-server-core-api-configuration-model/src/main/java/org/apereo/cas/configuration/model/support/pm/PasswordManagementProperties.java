package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
@NoArgsConstructor
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
     * A String value representing password policy regex pattern.
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

    /**
     * Settings related to fetching usernames.
     */
    private ForgotUsername forgotUsername = new ForgotUsername();

    /**
     * Settings related to password history management.
     */
    private PasswordHistory history = new PasswordHistory();
    
    /**
     * Handle password policy via Groovy script.
     */
    private Groovy groovy = new Groovy();

    @RequiresModule(name = "cas-server-support-pm-jdbc")
    @Getter
    @Setter
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
         * SQL query to locate the user phone number.
         */
        private String sqlFindPhone;

        /**
         * SQL query to locate the user via email.
         */
        private String sqlFindUser;

        /**
         * SQL query to locate security questions for the account, if any.
         */
        private String sqlSecurityQuestions;
    }

    @RequiresModule(name = "cas-server-support-pm-rest")
    @Getter
    @Setter
    public static class Rest implements Serializable {

        private static final long serialVersionUID = 5262948164099973872L;

        /**
         * Endpoint URL to use when locating email addresses.
         */
        private String endpointUrlEmail;

        /**
         * Endpoint URL to use when locating phone numbers.
         */
        private String endpointUrlPhone;

        /**
         * Endpoint URL to use when locating user names.
         */
        private String endpointUrlUser;

        /**
         * Endpoint URL to use when locating security questions.
         */
        private String endpointUrlSecurityQuestions;

        /**
         * Endpoint URL to use when updating passwords..
         */
        private String endpointUrlChange;

        /**
         * Username for Basic-Auth at the password management endpoints.
         */
        private String endpointUsername;

        /**
         * Password for Basic-Auth at the password management endpoints.
         */
        private String endpointPassword;
    }

    @RequiresModule(name = "cas-server-support-pm-ldap")
    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = -2610186056194686825L;

        /**
         * Collection of attribute names that indicate security questions answers.
         * This is done via a key-value structure where the key is the attribute name
         * for the security question and the value is the attribute name for the answer linked to the question.
         */
        private Map<String, String> securityQuestionsAttributes = new LinkedHashMap<>();

        /**
         * The specific variant of LDAP
         * based on which update operations will be constructed.
         */
        private LdapType type = LdapType.AD;

        /**
         * Username attribute required by LDAP.
         */
        private String usernameAttribute = "uid";
    }

    @RequiresModule(name = "cas-server-support-pm-webflow")
    @Getter
    @Setter
    public static class ForgotUsername implements Serializable {
        private static final long serialVersionUID = 4850199066765183587L;

        /**
         * Email settings for notifications.
         */
        @NestedConfigurationProperty
        private EmailProperties mail = new EmailProperties();

        public ForgotUsername() {
            this.mail.setAttributeName("mail");
            this.mail.setText("Your current username is: %s");
            this.mail.setSubject("Forgot Username");
        }
    }

    @RequiresModule(name = "cas-server-support-pm-webflow")
    @Getter
    @Setter
    public static class PasswordHistory implements Serializable {
        private static final long serialVersionUID = 2211199066765183587L;

        /**
         * Flag to indicate if password history tracking is enabled.
         */
        private boolean enabled;

        /**
         * Handle password history with Groovy.
         */
        @NestedConfigurationProperty
        private SpringResourceProperties groovy = new SpringResourceProperties();
    }
    
    @RequiresModule(name = "cas-server-support-pm-webflow")
    @Getter
    @Setter
    public static class Reset implements Serializable {

        private static final long serialVersionUID = 3453970349530670459L;

        /**
         * Crypto settings on how to reset the password.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        /**
         * Email settings for notifications.
         */
        @NestedConfigurationProperty
        private EmailProperties mail = new EmailProperties();

        /**
         * SMS settings for notifications.
         */
        @NestedConfigurationProperty
        private SmsProperties sms = new SmsProperties();

        /**
         * Whether reset operations require security questions,
         * or should they be marked as optional.
         */
        private boolean securityQuestionsEnabled = true;

        /**
         * How long in minutes should the password expiration link remain valid.
         */
        private long expirationMinutes = 1;

        public Reset() {
            mail.setAttributeName("mail");
            mail.setText("Reset your password via this link: %s");
            mail.setSubject("Password Reset");
            crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
            crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        }
    }

    @RequiresModule(name = "cas-server-support-pm")
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-pm")
    @Getter
    @Setter
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = 1129426669588789974L;
    }
}
