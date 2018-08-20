package org.apereo.cas.oidc.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.introspection.OidcIntrospectionAccessTokenResponse;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * This is {@link OidcIntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcIntrospectionEndpointController extends BaseOAuth20Controller {

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    public OidcIntrospectionEndpointController(final ServicesManager servicesManager,
            final TicketRegistry ticketRegistry, final AccessTokenFactory accessTokenFactory,
            final PrincipalFactory principalFactory,
            final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
            final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
            final CasConfigurationProperties casProperties, final CookieRetrievingCookieGenerator cookieGenerator,
            final CentralAuthenticationService centralAuthenticationService,
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, accessTokenFactory, principalFactory,
                webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties, cookieGenerator);
        this.centralAuthenticationService = centralAuthenticationService;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
    }

    /**
     * Handle request.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the response entity
     */
    @GetMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL })
    public ResponseEntity<OidcIntrospectionAccessTokenResponse> handleRequest(final HttpServletRequest request,
            final HttpServletResponse response) {
        return handlePostRequest(request, response);
    }

    /**
     * Handle post request.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return the response entity
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, value = {
            '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL })
    public ResponseEntity<OidcIntrospectionAccessTokenResponse> handlePostRequest(final HttpServletRequest request,
            final HttpServletResponse response) {
        try {
            final CredentialsExtractor<UsernamePasswordCredentials> authExtractor = new BasicAuthExtractor();
            final UsernamePasswordCredentials credentials = authExtractor
                    .extract(Pac4jUtils.getPac4jJ2EContext(request, response));
            if (credentials == null) {
                throw new IllegalArgumentException(
                        "No credentials are provided to verify introspection on the access token");
            }

            final OAuthRegisteredService service = OAuth20Utils
                    .getRegisteredOAuthServiceByClientId(this.servicesManager, credentials.getUsername());
            if (validateIntrospectionRequest(service, credentials, request)) {
                final String accessToken = StringUtils.defaultIfBlank(
                        request.getParameter(OAuth20Constants.ACCESS_TOKEN),
                        request.getParameter(OAuth20Constants.TOKEN));

                LOGGER.debug("Located access token [{}] in the request", accessToken);
                final AccessToken ticket = null;
                try {
                    this.centralAuthenticationService.getTicket(accessToken, AccessToken.class);
                } catch (final org.apereo.cas.ticket.InvalidTicketException ite) {
                    LOGGER.info("No ticket for supplied access token");
                }

                if (ticket != null) {
                    return createIntrospectionResponse(service, ticket);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean validateIntrospectionRequest(final OAuthRegisteredService registeredService,
            final UsernamePasswordCredentials credentials, final HttpServletRequest request) {
        final boolean tokenExists = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.ACCESS_TOKEN)
                || HttpRequestUtils.doesParameterExist(request, OAuth20Constants.TOKEN);

        if (tokenExists && OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword())) {
            final WebApplicationService service = webApplicationServiceServiceFactory
                    .createService(registeredService.getServiceId());
            final AuditableContext audit = AuditableContext.builder().service(service)
                    .registeredService(registeredService).build();
            final AuditableExecutionResult accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            return !accessResult.isExecutionFailure();
        }
        return false;
    }

    private ResponseEntity<OidcIntrospectionAccessTokenResponse> createIntrospectionResponse(
            final OAuthRegisteredService service, final AccessToken ticket) {

        final OidcIntrospectionAccessTokenResponse introspect = new OidcIntrospectionAccessTokenResponse();

        if (ticket != null) {
            introspect.setActive(true);
            introspect.setClientId(service.getClientId());
            final Authentication authentication = ticket.getAuthentication();
            final String subject = authentication.getPrincipal().getId();
            introspect.setSub(subject);
            introspect.setUniqueSecurityName(subject);
            introspect.setExp(ticket.getExpirationPolicy().getTimeToLive());
            introspect.setIat(ticket.getCreationTime().toInstant().getEpochSecond());

            final Object methods = authentication.getAttributes()
                    .get(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
            final String realmNames = CollectionUtils.toCollection(methods).stream().map(Object::toString)
                    .collect(Collectors.joining(","));

            introspect.setRealmName(realmNames);
            introspect.setTokenType(OAuth20Constants.TOKEN_TYPE_BEARER);

            final String grant = authentication.getAttributes()
                    .getOrDefault(OAuth20Constants.GRANT_TYPE, StringUtils.EMPTY).toString().toLowerCase();
            introspect.setGrantType(grant);
            introspect.setScope(OidcConstants.StandardScopes.OPENID.getScope());
            introspect.setAud(service.getServiceId());
            introspect.setIss(casProperties.getAuthn().getOidc().getIssuer());
        } else {
            introspect.setActive(false);
        }

        return new ResponseEntity<>(introspect, HttpStatus.OK);
    }
}
