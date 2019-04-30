package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeySetUtils;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;

/**
 * This is {@link OidcPrivateKeyJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcPrivateKeyJwtAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private final ServicesManager servicesManager;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final TicketRegistry ticketRegistry;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    @Override
    public void validate(final UsernamePasswordCredentials credentials,
                         final WebContext webContext) {
        if (!StringUtils.equalsIgnoreCase(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            credentials.getUsername())) {
            LOGGER.debug("client assertion type is not set to [{}]", OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
            return;
        }
        val code = webContext.getRequestParameter(OAuth20Constants.CODE);
        val oauthCode = this.ticketRegistry.getTicket(code, OAuthCode.class);
        if (oauthCode == null || oauthCode.isExpired()) {
            LOGGER.error("Provided code [{}] is either not found in the ticket registry or has expired", code);
            return;
        }
        val clientId = oauthCode.getClientId();
        val registeredService = (OidcRegisteredService)
            OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();

        val keys = OidcJsonWebKeySetUtils.getJsonWebKeySet(registeredService);
        keys.ifPresent(jwks ->
            jwks.getJsonWebKeys().forEach(jsonWebKey -> {
                val consumer = new JwtConsumerBuilder()
                    .setVerificationKey(jsonWebKey.getKey())
                    .setRequireSubject()
                    .setRequireJwtId()
                    .setRequireIssuedAt()
                    .build();
                try {
                    val jwt = consumer.processToClaims(credentials.getPassword());
                    val userProfile = new CommonProfile(true);
                    userProfile.setId(jwt.getSubject());
                    userProfile.addAttributes(jwt.getClaimsMap());
                    credentials.setUserProfile(userProfile);
                } catch (final Exception e) {
                    LOGGER.trace(e.getMessage(), e);
                }
            }));

    }
}
