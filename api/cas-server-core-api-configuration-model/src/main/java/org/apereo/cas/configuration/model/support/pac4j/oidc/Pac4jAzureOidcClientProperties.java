package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jAzureOidcClientProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jAzureOidcClientProperties extends BasePac4jOidcClientProperties {
    private static final long serialVersionUID = 1259382317533639638L;

    /**
     * Azure AD tenant name.
     * After tenant is configured, {@link #getDiscoveryUri()} property will be overridden.
     * <p>
     * Azure AD tenant name can take 4 different values:
     * <ul>
     * <li>{@code common}: Users with both a personal Microsoft account and a work or
     * school account from Azure AD can sign in. </li>
     * <li>{@code organizations}: Only users with work or school accounts from Azure
     * AD can sign in.</li>
     * <li>{@code consumers}: Only users with a personal Microsoft account can sign
     * in.</li>
     * <li>Specific tenant domain name or ID: Only user with account under that the
     * specified tenant can login</li>
     * </ul>
     */
    private String tenant;
}
