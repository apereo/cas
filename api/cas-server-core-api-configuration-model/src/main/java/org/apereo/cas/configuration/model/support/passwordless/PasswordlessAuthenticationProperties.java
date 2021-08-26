package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

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
@JsonFilter("PasswordlessAuthenticationProperties")
public class PasswordlessAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 8726382874579042117L;

    /**
     * Properties to instruct CAS how accounts for passwordless authentication should be located.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationAccountsProperties accounts = new PasswordlessAuthenticationAccountsProperties();

    /**
     * Properties to instruct CAS how tokens for passwordless authentication should be located.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationTokensProperties tokens = new PasswordlessAuthenticationTokensProperties();

    /**
     * Core passwordless settings.
     */
    @NestedConfigurationProperty
    private PasswordlessAuthenticationCoreProperties core = new PasswordlessAuthenticationCoreProperties();
    
}
