package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderSelectionProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MultifactorAuthenticationProviderSelectionProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 7426521468929733907L;

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

    /**
     * If provider selection is enabled, this setting will allow the user to skip the provider selection step
     * and proceed with authentication without having to select an MFA provider.
     */
    private boolean providerSelectionOptional;

    /**
     * Cookie settings that control how the selected mfa provider should be remembered.
     */
    @NestedConfigurationProperty
    private MultifactorAuthenticationProviderSelectionCookieProperties cookie = new MultifactorAuthenticationProviderSelectionCookieProperties();
}
