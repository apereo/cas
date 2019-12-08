package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public abstract class BaseIdTokenGeneratorService implements IdTokenGeneratorService {
    private final OAuth20ConfigurationContext configurationContext;

    /**
     * Gets authenticated profile.
     *
     * @param request  the request
     * @param response the response
     * @return the authenticated profile
     */
    protected UserProfile getAuthenticatedProfile(final HttpServletRequest request, final HttpServletResponse response) {
        val context = new JEEContext(request, response, getConfigurationContext().getSessionStore());
        val manager = new ProfileManager<>(context, context.getSessionStore());
        val profile = manager.get(true);

        if (profile.isEmpty()) {
            throw new IllegalArgumentException("Unable to determine the user profile from the context");
        }
        return profile.get();
    }

    /**
     * Encode and finalize token.
     *
     * @param claims            the claims
     * @param registeredService the registered service
     * @param accessToken       the access token
     * @return the string
     */
    protected String encodeAndFinalizeToken(final JwtClaims claims, final OAuthRegisteredService registeredService,
                                            final OAuth20AccessToken accessToken) {

        LOGGER.debug("Received claims for the id token [{}] as [{}]", accessToken, claims);
        val idTokenResult = getConfigurationContext().getIdTokenSigningAndEncryptionService().encode(registeredService, claims);
        accessToken.setIdToken(idTokenResult);

        LOGGER.debug("Updating access token [{}] in ticket registry with ID token [{}]", accessToken.getId(), idTokenResult);
        getConfigurationContext().getTicketRegistry().updateTicket(accessToken);
        return idTokenResult;
    }
}
