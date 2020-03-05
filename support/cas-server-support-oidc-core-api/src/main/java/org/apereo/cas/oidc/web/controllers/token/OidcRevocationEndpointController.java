package org.apereo.cas.oidc.web.controllers.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
            val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
            val token = context.getRequestParameter(OidcConstants.TOKEN)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            val accessToken = getOAuthConfigurationContext().getTicketRegistry().getTicket(token, OAuth20AccessToken.class);

            if (accessToken == null || accessToken.isExpired()) {
                throw new IllegalArgumentException("Provided refresh token [{}] is either not found in the ticket registry or has expired");
            }

            val tokenClientId = accessToken.getClientId();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getOAuthConfigurationContext().getServicesManager(),
                tokenClientId);
            val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());

            if (mustBeAuthenticated(registeredService)) {
                val authExtractor = new BasicAuthExtractor();
                val credentialsResult = authExtractor.extract(context);

                if (credentialsResult.isEmpty()) {
                    throw new IllegalArgumentException("No credentials are provided to verify revocation of the token");
                }

                val credentials = credentialsResult.get();
                if (OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword(), getOAuthConfigurationContext().getRegisteredServiceCipherExecutor())) {
                    throw new IllegalArgumentException("Unable to authenticate the client with the credentials provided");
                }
                if (!StringUtils.equals(credentials.getUsername(), tokenClientId)) {
                    throw new IllegalArgumentException("Provided access token is not related with the credentials provided");
                }
            }

            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);

            if (!accessResult.isExecutionFailure() && !StringUtils.isEmpty(tokenClientId)) {
                LOGGER.debug("Located token [{}] in the revocation request", token);
                getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    protected boolean mustBeAuthenticated(final OAuthRegisteredService registeredService) {

        return !StringUtils.isBlank(registeredService.getClientSecret());
    }
}
