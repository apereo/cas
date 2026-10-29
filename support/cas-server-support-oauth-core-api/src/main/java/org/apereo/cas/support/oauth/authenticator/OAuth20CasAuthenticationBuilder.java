package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20CasAuthenticationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface OAuth20CasAuthenticationBuilder {
    /**
     * Build service.
     *
     * @param registeredService the registered service
     * @param request           the request
     * @param useServiceHeader  the use service header
     * @return the service
     */
    Service buildService(OAuthRegisteredService registeredService,
                         HttpServletRequest request, boolean useServiceHeader);

    /**
     * Create an authentication from a user profile.
     * The roles of the profile are copied into a {@link LinkedHashSet}, which Jackson can deserialize again.
     *
     * @param profile           the given user profile
     * @param registeredService the registered service
     * @param request           the request
     * @param service           the service
     * @return the built authentication
     * @throws Throwable the throwable
     */
    Authentication build(OAuth20UserProfile profile,
                         OAuthRegisteredService registeredService,
                         HttpServletRequest request,
                         Service service)
            throws Throwable;
}
