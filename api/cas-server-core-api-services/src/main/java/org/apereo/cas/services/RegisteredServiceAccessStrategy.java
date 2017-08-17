package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RegisteredServiceAccessStrategy}
 * that can decide if a service is recognized and authorized to participate
 * in the CAS protocol flow during authentication/validation events.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceAccessStrategy extends Serializable {

    /**
     * Verify is the service is enabled and recognized by CAS.
     *
     * @return true /false if service is enabled
     */
    boolean isServiceAccessAllowed();

    /**
     * Assert that the service can participate in sso.
     *
     * @return true /false if service can participate in sso
     */
    boolean isServiceAccessAllowedForSso();

    /**
     * Verify authorization policy by checking the pre-configured rules
     * that may depend on what the principal might be carrying.
     *
     * @param principal           The authenticated principal
     * @param principalAttributes the principal attributes. Rather than passing the principal directly, we are only allowing principal attributes
     *                            given they may be coming from a source external to the principal itself. (Cached principal attributes, etc)
     * @return true /false if service access can be granted to principal
     */
    boolean doPrincipalAttributesAllowServiceAccess(String principal, Map<String, Object> principalAttributes);

    /**
     * Redirect the request to a separate and possibly external URL
     * in case authorization fails for this service. If no URL is
     * specified, CAS shall redirect the request by default to a generic
     * page that describes the authorization failed attempt.
     *
     * @return the redirect url
     * @since 4.2
     */
    URI getUnauthorizedRedirectUrl();

    /**
     * Subordinate access strategies are secondary auxiliary strategies
     * that are specifically used by extensions. Rather than having extensions
     * and modules implement their own access strategy, thereby losing defaults,
     * this is an approach to have each implementation tie an instance directly
     * yet loosely into the API. It is up to the caller/extension to enforce
     * subordinate access strategies.
     *
     * @return the subordinates
     */
    default Set<RegisteredServiceAccessStrategy> getSubordinates() {
        return new LinkedHashSet<>(0);
    }
}
