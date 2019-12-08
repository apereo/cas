package org.apereo.cas.oidc.authn;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;

import java.util.Optional;

/**
 * This is {@link OidcAccessTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcAccessTokenAuthenticator extends OAuth20AccessTokenAuthenticator {
    private final OAuth20TokenSigningAndEncryptionService idTokenSigningAndEncryptionService;
    private final ServicesManager servicesManager;

    public OidcAccessTokenAuthenticator(final TicketRegistry ticketRegistry,
                                        final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService,
                                        final ServicesManager servicesManager,
                                        final JwtBuilder accessTokenJwtBuilder) {
        super(ticketRegistry, accessTokenJwtBuilder);
        this.idTokenSigningAndEncryptionService = signingAndEncryptionService;
        this.servicesManager = servicesManager;
    }

    @Override
    protected CommonProfile buildUserProfile(final TokenCredentials tokenCredentials, final WebContext webContext, final OAuth20AccessToken accessToken) {
        try {
            val profile = super.buildUserProfile(tokenCredentials, webContext, accessToken);
            validateIdTokenIfAny(accessToken, profile);
            return profile;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Validate id token if any.
     *
     * @param accessToken the access token
     * @param profile     the profile
     * @throws MalformedClaimException the malformed claim exception
     */
    protected void validateIdTokenIfAny(final OAuth20AccessToken accessToken, final CommonProfile profile) throws MalformedClaimException {
        if (StringUtils.isNotBlank(accessToken.getIdToken())) {
            val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, accessToken.getClientId());
            val idTokenResult = idTokenSigningAndEncryptionService.decode(accessToken.getIdToken(), Optional.ofNullable(service));
            profile.setId(idTokenResult.getSubject());
            profile.addAttributes(idTokenResult.getClaimsMap());
        }
    }
}
