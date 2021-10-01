package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link BaseIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Getter
public abstract class BaseIdTokenGeneratorService<T extends OAuth20ConfigurationContext> implements IdTokenGeneratorService {
    private final ObjectProvider<T> configurationContextProvider;

    protected T getConfigurationContext() {
        return this.configurationContextProvider.getObject();
    }

    /**
     * Gets authenticated profile.
     *
     * @param context the context
     * @return the authenticated profile
     */
    protected UserProfile getAuthenticatedProfile(final WebContext context) {
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        val profile = manager.getProfile();

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
