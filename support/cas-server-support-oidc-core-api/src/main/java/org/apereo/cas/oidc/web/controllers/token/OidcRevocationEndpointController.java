package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcRevocationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcRevocationEndpointController extends BaseOAuth20Controller {
    public OidcRevocationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request for revocation.
     *
     * @param request  the request
     * @param response the response
     * @return the jwk set
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL)
    public ResponseEntity<String> handleRequestInternal(final HttpServletRequest request,
                                                        final HttpServletResponse response) {
        try {
            val authExtractor = new BasicAuthExtractor();
            val credentialsResult = authExtractor.extract(new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore()));
            if (credentialsResult.isEmpty()) {
                throw new IllegalArgumentException("No credentials are provided to verify revocation of the token");
            }

            val credentials = credentialsResult.get();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getOAuthConfigurationContext().getServicesManager(),
                credentials.getUsername());
            val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());

            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);

            if (!accessResult.isExecutionFailure()
                && HttpRequestUtils.doesParameterExist(request, OidcConstants.TOKEN)
                && OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword(),
                getOAuthConfigurationContext().getRegisteredServiceCipherExecutor())) {
                val token = request.getParameter(OidcConstants.TOKEN);
                LOGGER.debug("Located token [{}] in the revocation request", token);
                getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
