package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.UserProfile;

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
     * @param context           the context
     * @param useServiceHeader  the use service header
     * @return the service
     */
    Service buildService(OAuthRegisteredService registeredService,
                         JEEContext context, boolean useServiceHeader);

    /**
     * Create an authentication from a user profile.
     * pac4j {@code UserProfile.getPermissions()} and {@code getRoles()} returns
     * {@code UnmodifiableSet} which Jackson Serializer happily serializes to json but is unable to deserialize.
     * We have to transform those to HashSet to avoid such a problem.
     *
     * @param profile           the given user profile
     * @param registeredService the registered service
     * @param context           the context
     * @param service           the service
     * @return the built authentication
     */
    Authentication build(UserProfile profile,
                         OAuthRegisteredService registeredService,
                         JEEContext context,
                         Service service);
}
