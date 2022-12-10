package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * This is {@link OidcJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcJwtAuthenticator implements Authenticator {
    /**
     * OIDC issuer service.
     */
    protected final OidcIssuerService issuerService;

    /**
     * Services Manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Registered service access strategy.
     */
    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    /**
     * Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    /**
     * CAS properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Resource loader instance.
     */
    protected final ApplicationContext applicationContext;

    protected OidcRegisteredService verifyCredentials(final UsernamePasswordCredentials credentials,
                                                      final WebContext webContext) {
        if (!StringUtils.equalsIgnoreCase(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            credentials.getUsername())) {
            LOGGER.debug("client assertion type is not set to [{}]", OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
            return null;
        }
        if (StringUtils.isBlank(credentials.getPassword())) {
            LOGGER.debug("No assertion is available in the provided credentials");
            return null;
        }

        try {
            val jwt = JWTParser.parse(credentials.getPassword());
            val alg = jwt.getHeader().getAlgorithm();
            if (!validateJwtAlgorithm(alg)) {
                LOGGER.debug("No assertion is available in the provided credentials");
                return null;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return null;
        }

        val code = webContext.getRequestParameter(OAuth20Constants.CODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val oauthCode = FunctionUtils.doAndHandle(() -> {
            val state = ticketRegistry.getTicket(code, OAuth20Code.class);
            return state == null || state.isExpired() ? null : state;
        });
        val clientId = oauthCode == null ? webContext.getRequestParameter(OAuth20Constants.CLIENT_ID).get() : oauthCode.getClientId();
        val registeredService = (OidcRegisteredService) OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
        if (accessResult.isExecutionFailure()) {
            return null;
        }
        return registeredService;
    }

    @Override
    public Optional<Credentials> validate(final Credentials creds,
                                          final WebContext webContext,
                                          final SessionStore sessionStore) {

        val credentials = (UsernamePasswordCredentials) creds;
        val registeredService = verifyCredentials(credentials, webContext);
        if (registeredService == null) {
            LOGGER.warn("Unable to verify credentials");
            return Optional.empty();
        }

        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService,
            applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING));
        keys.ifPresent(Unchecked.consumer(jwks ->
            jwks.getJsonWebKeys()
                .forEach(Unchecked.consumer(jsonWebKey -> {
                    val consumer = new JwtConsumerBuilder()
                        .setVerificationKey(jsonWebKey.getKey())
                        .setRequireJwtId()
                        .setRequireExpirationTime()
                        .setRequireSubject()
                        .setExpectedIssuer(true, issuerService.determineIssuer(Optional.of(registeredService)))
                        .setExpectedAudience(true, registeredService.getClientId())
                        .build();
                    determineUserProfile(credentials, consumer);
                }))));
        return Optional.of(credentials);
    }


    protected void determineUserProfile(final UsernamePasswordCredentials credentials,
                                        final JwtConsumer consumer) throws Exception {
        FunctionUtils.doAndHandle(c -> {
            val jwt = consumer.processToClaims(credentials.getPassword());
            val userProfile = new CommonProfile(true);
            userProfile.setId(jwt.getSubject());
            userProfile.addAttributes(jwt.getClaimsMap());
            credentials.setUserProfile(userProfile);
        });
    }

    protected boolean validateJwtAlgorithm(final Algorithm alg) {
        return JWSAlgorithm.Family.HMAC_SHA.contains(alg)
               || JWSAlgorithm.Family.RSA.contains(alg)
               || JWSAlgorithm.Family.EC.contains(alg);
    }
}
