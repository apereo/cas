package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.support.replication.CookieSessionReplicationProperties;
import org.apereo.cas.configuration.model.support.replication.SessionReplicationProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Pac4jDelegatedAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -3561947621312270068L;

    /**
     * When constructing the final user profile from
     * the delegated provider, determines if the provider id
     * should be combined with the principal id.
     */
    private boolean typedIdUsed;

    /**
     * The attribute to use as the principal identifier built during and upon a successful authentication attempt.
     */
    private String principalIdAttribute;

    /**
     * Whether initialization of delegated identity providers should be done
     * eagerly typically during startup.
     */
    private boolean lazyInit = true;

    /**
     * The name of the authentication handler in CAS used for delegation.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;


    /**
     * Control the expiration policy of the cache
     * that holds onto the results.
     */
    @DurationCapable
    private String cacheDuration = "PT8H";

    /**
     * Control the size of the delegated identity provider cache
     * that holds identity providers.
     * <p>
     * This setting specifies the maximum number of entries the cache may contain. Note that the cache <b>may evict
     * an entry before this limit is exceeded or temporarily exceed the threshold while evicting</b>.
     * As the cache size grows close to the maximum, the cache evicts entries that are less likely to
     * be used again. For example, the cache may evict an entry because it hasn't been used recently
     * or very often.
     */
    private long cacheSize = 100;

    /**
     * Control settings for session replication.
     */
    @NestedConfigurationProperty
    private SessionReplicationProperties sessionReplication = new SessionReplicationProperties();

    /**
     * Path to a groovy script to determine the auto-redirection
     * strategy to identity providers.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovyRedirectionStrategy = new SpringResourceProperties();

    /**
     * Path to a groovy script to post-process identity providers
     * before they are presented to the user.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovyProviderPostProcessor = new SpringResourceProperties();

    /**
     * Path to a groovy script to customize the authentication request
     * and the configuration responsible for it before the
     * request is handed off to the identity provider.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovyAuthenticationRequestCustomizer = new SpringResourceProperties();

    /**
     * Discovery selection settings.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationDiscoverySelectionProperties discoverySelection = new Pac4jDelegatedAuthenticationDiscoverySelectionProperties();

    public Pac4jDelegatedAuthenticationCoreProperties() {
        if (StringUtils.isBlank(getSessionReplication().getCookie().getName())) {
            getSessionReplication().getCookie()
                .setName("%s%s".formatted(CookieSessionReplicationProperties.DEFAULT_COOKIE_NAME, "AuthnDelegation"));
        }
    }
}
