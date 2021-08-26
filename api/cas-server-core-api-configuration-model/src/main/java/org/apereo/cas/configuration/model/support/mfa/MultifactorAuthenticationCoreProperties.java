package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
@JsonFilter("MultifactorAuthenticationCoreProperties")
public class MultifactorAuthenticationCoreProperties implements Serializable {
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
    private String contentType = "application/cas";

    /**
     * In the event that multiple multifactor authentication
     * providers are determined for a multifactor authentication transaction,
     * by default CAS will attempt to sort the collection of providers based on their rank and
     * will pick one with the highest priority. This use case may arise if multiple triggers
     * are defined where each decides on a different multifactor authentication provider, or
     * the same provider instance is configured multiple times with many instances.
     * Provider selection may also be carried out using Groovy scripting strategies more dynamically.
     * The following example should serve as an outline of how to select multifactor providers based on a Groovy script.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties providerSelectorGroovyScript = new SpringResourceProperties();

    /**
     * In the event that multiple multifactor authentication providers are determined for a multifactor authentication transaction,
     * this setting will allow one to interactively choose a provider out of the list of available providers.
     * A trigger may be designed to support more than one provider, and rather than letting CAS auto-determine
     * the selected provider via scripts or ranking strategies, this method puts the choice back onto the user
     * to decide which provider makes the most sense at any given time.
     */
    private boolean providerSelectionEnabled;
}
