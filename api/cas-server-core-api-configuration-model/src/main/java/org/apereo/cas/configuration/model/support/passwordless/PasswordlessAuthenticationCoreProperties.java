package org.apereo.cas.configuration.model.support.passwordless;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link PasswordlessAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-passwordless")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PasswordlessAuthenticationCoreProperties")
public class PasswordlessAuthenticationCoreProperties implements Serializable {
    private static final long serialVersionUID = 6726382874579042117L;

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

}
