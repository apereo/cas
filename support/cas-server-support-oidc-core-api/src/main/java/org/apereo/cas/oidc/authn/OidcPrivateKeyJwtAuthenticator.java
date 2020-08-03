package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link OidcPrivateKeyJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcPrivateKeyJwtAuthenticator extends BaseOidcJwtAuthenticator {

    public OidcPrivateKeyJwtAuthenticator(final ServicesManager servicesManager,
                                          final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                          final TicketRegistry ticketRegistry,
                                          final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                          final CasConfigurationProperties casProperties,
                                          final ApplicationContext applicationContext) {
        super(servicesManager, registeredServiceAccessStrategyEnforcer,
            ticketRegistry, webApplicationServiceServiceFactory, casProperties, applicationContext);
    }

    @Override
    protected boolean validateJwtAlgorithm(final Algorithm alg) {
        return JWSAlgorithm.Family.RSA.contains(alg) || JWSAlgorithm.Family.EC.contains(alg);
    }

    @Override
    public void validate(final UsernamePasswordCredentials credentials,
                         final WebContext webContext) {

        val registeredService = verifyCredentials(credentials, webContext);
        if (registeredService == null) {
            LOGGER.warn("Unable to verify credentials");
            return;
        }

        val clientId = registeredService.getClientId();
        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL);
        val keys = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService, this.applicationContext);
        keys.ifPresent(jwks ->
            jwks.getJsonWebKeys().forEach(jsonWebKey -> {
                val consumer = new JwtConsumerBuilder()
                    .setVerificationKey(jsonWebKey.getKey())
                    .setRequireSubject()
                    .setExpectedSubject(clientId)
                    .setRequireJwtId()
                    .setRequireExpirationTime()
                    .setExpectedIssuer(true, clientId)
                    .setExpectedAudience(true, audience)
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
