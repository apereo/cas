package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MultifactorAuthenticationCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 7426521468929733907L;

    /**
     * Attribute returned in the final CAS validation payload
     * that indicates the authentication context class satisfied
     * in the event of a multifactor authentication attempt.
     */
    private String authenticationContextAttribute = "authnContextClass";

    /**
     * Defines the global failure mode for the entire deployment.
     * This is meant to be used a shortcut to define the policy globally
     * rather than per application. Applications registered with CAS can still
     * define a failure mode and override the global.
     */
    private MultifactorAuthenticationProviderFailureModes globalFailureMode = MultifactorAuthenticationProviderFailureModes.CLOSED;

    /**
     * Content-type that is expected to be specified by non-web clients such as curl, etc in the
     * event that the provider supports variations of non-browser based MFA.
     * The value is treated as a regular expression.
     */
    @RegularExpressionCapable
    private String contentType = "application/cas";

    /**
     * In the event that multiple multifactor authentication providers are determined for
     * a multifactor authentication transaction, the collection of settings here control mfa selection rules.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationProviderSelectionProperties providerSelection = new MultifactorAuthenticationProviderSelectionProperties();
}
