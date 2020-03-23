package org.apereo.cas.configuration.model.support.azuread;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link AzureActiveDirectoryAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-azuread-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AzureActiveDirectoryAttributesProperties implements Serializable {

    private static final long serialVersionUID = -12055975558426360L;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * Whether attribute repository should consider the underlying
     * attribute names in a case-insensitive manner.
     */
    private boolean caseInsensitive;

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * The microsoft tenant id.
     */
    private String tenant;

    /**
     * Resource to fetch access tokens for; defaults to the graph api url.
     */
    private String resource;

    /**
     * Scope used when fetching access tokens.
     */
    private String scope;

    /**
     * Grant type used to fetch access tokens; defaults to {@code client_credentials}.
     */
    private String grantType;

    /**
     * Comma-separated attributes and user properties to fetch from microsoft graph.
     * If attributes are specified here, they would be the only ones requested and fetched.
     * If this field is left blank, a default set of attributes are fetched and returned.
     */
    private String attributes;

    /**
     * Base API url used to contact microsoft graph for calls.
     */
    private String apiBaseUrl;

    /**
     * Base login url used to fetch access tokens.
     */
    private String loginBaseUrl;

    /**
     * Domain that is appended to usernames when doing lookups.
     * The {@code @} is automatically included.
     */
    private String domain;

    /**
     * Adjust the logging level of the API calls. Defaults to {@code basic}.
     * Accepted values are {@code none,headers,basic,body}.
     */
    private String loggingLevel;

    /**
     * Client id of the registered app in microsoft azure portal.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Client secret of the registered app in microsoft azure portal.
     */
    @RequiredProperty
    private String clientSecret;
}
