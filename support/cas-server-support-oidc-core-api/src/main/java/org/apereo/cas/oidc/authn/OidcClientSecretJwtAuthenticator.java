package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.auth.ClientSecretJWT;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;

/**
 * This is {@link OidcClientSecretJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcClientSecretJwtAuthenticator extends BaseOidcJwtAuthenticator {

    public OidcClientSecretJwtAuthenticator(final ServicesManager servicesManager,
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
        return JWSAlgorithm.Family.HMAC_SHA.contains(alg);
    }

    @Override
    public void validate(final UsernamePasswordCredentials credentials,
                         final WebContext webContext) {
        val registeredService = verifyCredentials(credentials, webContext);
        if (registeredService == null) {
            LOGGER.warn("Unable to verify credentials");
            return;
        }
        try {

            val params = new HashMap<String, List<String>>();
            params.put(OAuth20Constants.CLIENT_ASSERTION_TYPE, List.of(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER));
            params.put(OAuth20Constants.CLIENT_ASSERTION, List.of(credentials.getPassword()));
            val jwt = ClientSecretJWT.parse(params);
            val userProfile = new CommonProfile(true);
            userProfile.setId(jwt.getClientID().getValue());
            credentials.setUserProfile(userProfile);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

    }
}
