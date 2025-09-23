package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationGroovyAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationJsonAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationLdapAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationMongoDbAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationRestAccountsProperties;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationSyncopeAccountsProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PasswordlessAuthenticationAccountsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-passwordless-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationAccountsProperties implements Serializable {

    @Serial
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
     * Passwordless authentication settings via JSON resource.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationJsonAccountsProperties json = new PasswordlessAuthenticationJsonAccountsProperties();

    /**
     * Passwordless authentication settings via MongoDb.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationMongoDbAccountsProperties mongo = new PasswordlessAuthenticationMongoDbAccountsProperties();

    /**
     * Passwordless authentication settings via Apache Syncope.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationSyncopeAccountsProperties syncope = new PasswordlessAuthenticationSyncopeAccountsProperties();

    /**
     * Passwordless authentication settings using static accounts.
     * The key is the user identifier, while the value is the form of
     * contact such as email, sms, etc.
     */
    private Map<String, String> simple = new LinkedHashMap<>(2);
}

