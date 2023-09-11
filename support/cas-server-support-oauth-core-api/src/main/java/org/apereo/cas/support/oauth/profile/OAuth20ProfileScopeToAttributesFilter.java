package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ProfileScopeToAttributesFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OAuth20ProfileScopeToAttributesFilter {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "profileScopeToAttributesFilter";

    default Map<String, ? extends RegisteredServiceAttributeReleasePolicy> getAttributeReleasePolicies() {
        return new LinkedHashMap<>(0);
    }

    /**
     * Filter attributes.
     *
     * @param service           the service
     * @param profile           the profile
     * @param registeredService the registered service
     * @param accessToken       the access token
     * @return the map
     * @throws Throwable the throwable
     */
    default Principal filter(final Service service, final Principal profile,
                             final RegisteredService registeredService,
                             final OAuth20AccessToken accessToken) throws Throwable {
        return profile;
    }

    /**
     * Filter principal.
     *
     * @param service           the service
     * @param profile           the profile
     * @param registeredService the registered service
     * @return the principal
     * @throws Throwable the throwable
     */
    default Principal filter(final Service service, final Principal profile,
                             final RegisteredService registeredService) throws Throwable {
        return filter(service, profile, registeredService, null);
    }
}
