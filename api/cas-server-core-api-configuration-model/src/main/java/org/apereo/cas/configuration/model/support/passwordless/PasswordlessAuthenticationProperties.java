package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationGroovyAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationLdapAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationMongoDbAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationJpaTokensProperties;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
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
     * <p>
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
     * <p>
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
    @Accessors(chain = true)
    public static class Accounts implements Serializable {

        private static final long serialVersionUID = -8424650395669337488L;

        /**
         * Passwordless authentication settings via REST.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationRestAccountsProperties rest = new PasswordlessAuthenticationRestAccountsProperties();

        /**
         * Passwordless authentication settings via LDAP.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationLdapAccountsProperties ldap = new PasswordlessAuthenticationLdapAccountsProperties();

        /**
         * Passwordless authentication settings via Groovy.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationGroovyAccountsProperties groovy = new PasswordlessAuthenticationGroovyAccountsProperties();

        /**
         * Passwordless authentication settings via MongoDb.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationMongoDbAccountsProperties mongo = new PasswordlessAuthenticationMongoDbAccountsProperties();

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
    @Accessors(chain = true)
    public static class Tokens implements Serializable {

        private static final long serialVersionUID = 8371063350377031703L;

        /**
         * Indicate how long should the token be considered valid.
         */
        private int expireInSeconds = 180;

        /**
         * Passwordless authentication settings via REST.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationRestTokensProperties rest = new PasswordlessAuthenticationRestTokensProperties();

        /**
         * Passwordless authentication settings via JPA.
         */
        @NestedConfigurationProperty
        private PasswordlessAuthenticationJpaTokensProperties jpa = new PasswordlessAuthenticationJpaTokensProperties();

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


}
