package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PasswordlessAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-passwordless")
@Getter
@Setter
public class PasswordlessAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 8726382874579042117L;

    /**
     * Properties to instruct CAS how accounts for passwordless authentication should be located.
     */
    private Accounts accounts = new Accounts();

    /**
     * Properties to instruct CAS how tokens for passwordless authentication should be located.
     */
    private Tokens tokens = new Tokens();

    /**
     * Allow passwordless authentication to skip its own flow
     * in favor of multifactor authentication providers that may be available
     * and defined in CAS.
     * 
     * If multifactor authentication is activated, and defined MFA triggers
     * in CAS signal availability and eligibility of an MFA flow for
     * the given passwordless user, CAS will skip its normal passwordless
     * authentication flow in favor of the requested multifactor authentication
     * provider. If no MFA providers are available, or if no triggers require
     * MFA for the verified passwordless user, passwordless authentication flow
     * will commence as usual.
     */
    private boolean multifactorAuthenticationActivated;

    /**
     * Allow passwordless authentication to skip its own flow
     * in favor of delegated authentication providers that may be available
     * and defined in CAS.
     *
     * If delegated authentication is activated, CAS will skip its normal passwordless
     * authentication flow in favor of the requested delegated authentication
     * provider. If no delegated providers are available, passwordless authentication flow
     * will commence as usual.
     */
    private boolean delegatedAuthenticationActivated;

    /**
     * Select the delegated identity provider for the passwordless
     * user using a script.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties delegatedAuthenticationSelectorScript = new SpringResourceProperties();
    
    @RequiresModule(name = "cas-server-support-passwordless")
    @Getter
    @Setter
    public static class Accounts implements Serializable {

        private static final long serialVersionUID = -8424650395669337488L;

        /**
         * Passwordless authentication settings via REST.
         */
        private Rest rest = new Rest();

        /**
         * Passwordless authentication settings via LDAP.
         */
        private Ldap ldap = new Ldap();

        /**
         * Passwordless authentication settings via Groovy.
         */
        private Groovy groovy = new Groovy();

        /**
         * Passwordless authentication settings using static accounts.
         * The key is the user identifier, while the value is the form of
         * contact such as email, sms, etc.
         */
        private Map<String, String> simple = new LinkedHashMap<>(2);
    }

    @RequiresModule(name = "cas-server-support-passwordless")
    @Getter
    @Setter
    public static class Tokens implements Serializable {

        private static final long serialVersionUID = 8371063350377031703L;

        /**
         * Indicate how long should the token be considered valid.
         */
        private int expireInSeconds = 180;

        /**
         * Passwordless authentication settings via REST.
         */
        private RestTokens rest = new RestTokens();

        /**
         * Passwordless authentication settings via JPA.
         */
        private Jpa jpa = new Jpa();

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
    }

    @RequiresModule(name = "cas-server-support-passwordless")
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-passwordless-ldap")
    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {
        private static final long serialVersionUID = -1102345678378393382L;

        /**
         * Name of the LDAP attribute that
         * indicates the user's email address.
         */
        private String emailAttribute = "mail";

        /**
         * Name of the LDAP attribute that
         * indicates the user's phone.
         */
        private String phoneAttribute = "phoneNumber";
    }

    @RequiresModule(name = "cas-server-support-passwordless-jpa")
    @Getter
    @Setter
    public static class Jpa extends AbstractJpaProperties {

        private static final long serialVersionUID = 7647381223153797806L;

        /**
         * Settings that control the background cleaner process.
         */
        @NestedConfigurationProperty
        private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT15S", "PT2M");
    }

    @RequiresModule(name = "cas-server-support-passwordless")
    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }

    @RequiresModule(name = "cas-server-support-passwordless")
    @Getter
    @Setter
    public static class RestTokens extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;

        /**
         * Crypto settings on how to reset the password.
         */
        @NestedConfigurationProperty
        private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

        public RestTokens() {
            crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
            crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        }
    }
}
