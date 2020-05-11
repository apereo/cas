package org.apereo.cas.oidc.authn;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link BaseOidcJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseOidcJwtAuthenticator implements Authenticator<UsernamePasswordCredentials> {

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

    /** Resource loader instance. */
    protected final ApplicationContext applicationContext;

    /**
     * Verify credentials and fetch oidc registered service.
     *
     * @param credentials the credentials
     * @param webContext  the web context
     * @return the oidc registered service
     */
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
            LOGGER.error(e.getMessage(), e);
            return null;
        }

        val code = webContext.getRequestParameter(OAuth20Constants.CODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val oauthCode = ticketRegistry.getTicket(code, OAuth20Code.class);
        if (oauthCode == null || oauthCode.isExpired()) {
            LOGGER.error("Provided code [{}] is either not found in the ticket registry or has expired", code);
            return null;
        }
        val clientId = oauthCode.getClientId();
        val registeredService = (OidcRegisteredService)
            OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        if (accessResult.isExecutionFailure()) {
            return null;
        }
        return registeredService;
    }

    /**
     * Validate jwt algorithm and return true/false.
     *
     * @param alg the alg
     * @return true/false
     */
    protected abstract boolean validateJwtAlgorithm(Algorithm alg);
}
