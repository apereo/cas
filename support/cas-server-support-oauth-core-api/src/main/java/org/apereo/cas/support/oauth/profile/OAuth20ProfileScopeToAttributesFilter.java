package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import org.pac4j.core.context.J2EContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OAuth20ProfileScopeToAttributesFilter {

    default Map<String, ? extends RegisteredServiceAttributeReleasePolicy> getAttributeReleasePolicies() {
        return new LinkedHashMap<>();
    }

    /**
     * Filter attributes.
     *
     * @param service           the service
     * @param profile           the profile
     * @param registeredService the registered service
     * @param context           the context
     * @param accessToken       the access token
     * @return the map
     */
    default Principal filter(final Service service, final Principal profile,
                             final RegisteredService registeredService, final J2EContext context,
                             final AccessToken accessToken) {
        return profile;
    }
}
