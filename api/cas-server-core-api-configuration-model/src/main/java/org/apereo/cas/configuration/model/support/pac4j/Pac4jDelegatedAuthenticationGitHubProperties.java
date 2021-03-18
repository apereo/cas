package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationGitHubProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationGitHubProperties extends Pac4jIdentifiableClientProperties {

    private static final long serialVersionUID = -5663033494303169583L;

    /**
     * The requested scope from the provider.
     *  The default scope is {@code user}, i.e. {@code read/write} access to the GitHub user account.
     *  For a full list of possible scopes, <a href="https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/">see this</a>).
     */
    private String scope;

    public Pac4jDelegatedAuthenticationGitHubProperties() {
        setClientName("GitHub");
    }
}
