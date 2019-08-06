package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
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
         * Passwordless authentication settings via Groovy.
         */
        private Groovy groovy = new Groovy();

        /**
         * Passwordless authentication settings using static accounts.
         * The key is the user identifier, while the value is the form of
         * contact such as email, sms, etc.
         */
        private Map<String, String> simple = new LinkedHashMap<>();
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
