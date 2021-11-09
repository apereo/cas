package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
@JsonFilter("Pac4jDelegatedAuthenticationCoreProperties")
public class Pac4jDelegatedAuthenticationCoreProperties implements Serializable {
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
    private String principalAttributeId;

    /**
     * Whether initialization of delegated identity providers should be done
     * eagerly typically during startup.
     */
    private boolean lazyInit = true;

    /**
     * Indicates whether profiles and other session data,
     * collected as part of pac4j flows and requests
     * that are kept by the container session, should be replicated
     * across the cluster using CAS and its own ticket registry.
     * Without this option, profile data and other related
     * pieces of information should be manually replicated
     * via means and libraries outside of CAS.
     */
    private boolean replicateSessions = true;

    /**
     * The name of the authentication handler in CAS used for delegation.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;

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
     * Discovery selection settings.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationDiscoverySelectionProperties discoverySelection = new Pac4jDelegatedAuthenticationDiscoverySelectionProperties();

}
